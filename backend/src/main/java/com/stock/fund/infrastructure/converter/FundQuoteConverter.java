package com.stock.fund.infrastructure.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.infrastructure.client.SinaFundApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 基金净值数据转换器
 * 
 * 负责将 API 原始数据转换为领域实体，遵循 DDD 基础设施层职责划分。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FundQuoteConverter {

    private final SinaFundApiClient sinaFundApiClient;

    /**
     * 将基金 API 数据转换为 FundQuote 实体
     * 
     * @param target   采集目标
     * @param fundData 基金数据（来自 SinaFundApiClient）
     * @return FundQuote 实体
     */
    public FundQuote toFundQuote(DataCollectionTarget target, java.util.Map<String, String> fundData) {
        FundQuote fundQuote = new FundQuote();

        // 设置基金代码
        fundQuote.setFundCode(target.getCode());

        // 设置基金名称
        fundQuote.setFundName(target.getName());

        // 解析净值 - 使用BigDecimal避免精度问题
        BigDecimal nav = parseNav(fundData.get("netValue"));
        fundQuote.setNav(nav);

        // 解析昨日净值并计算涨跌幅
        ParsedOldNetValue result = parseOldNetValueAndChangePercent(fundData.get("oldNetValue"), nav);
        fundQuote.setPrevNetValue(result.oldNetValue);
        fundQuote.setChangePercent(result.changePercent);

        // 计算涨跌额 (当前净值 - 昨日净值) - 4位小数
        BigDecimal changeAmount = nav.subtract(result.oldNetValue).setScale(4, RoundingMode.HALF_UP);
        fundQuote.setChangeAmount(changeAmount);

        // 设置日期和时间
        QuoteDateTime dateTime = parseQuoteDateTime(fundData.get("time"));
        fundQuote.setQuoteDate(dateTime.date);
        fundQuote.setQuoteTimeOnly(dateTime.time);

        return fundQuote;
    }

    /**
     * 从基金实时数据中提取基础信息
     * 
     * @param fundQuote 基金实时数据
     * @return Fund 基金基础信息
     */
    public Fund toFundBasicInfo(FundQuote fundQuote) {
        Fund fund = new Fund();
        fund.setFundCode(fundQuote.getFundCode());

        // 使用从API获取的基金名称
        fund.setName(fundQuote.getFundName());

        // 从实时数据中提取净值作为基础信息
        fund.setNav(fundQuote.getNav());
        fund.setDayGrowth(fundQuote.getChangePercent());

        // 设置默认值
        fund.setType("未知"); // 实时数据中通常不包含基金类型
        fund.setManager("未知"); // 实时数据中通常不包含基金经理
        fund.setEstablishmentDate(LocalDate.now()); // 使用当前日期作为默认成立日期
        fund.setFundSize(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)); // 实时数据中通常不包含基金规模
        fund.setWeekGrowth(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        fund.setMonthGrowth(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        fund.setYearGrowth(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        return fund;
    }

    private BigDecimal parseNav(String netValueStr) {
        try {
            if (netValueStr != null && !netValueStr.trim().isEmpty()) {
                return new BigDecimal(netValueStr).setScale(4, RoundingMode.HALF_UP);
            }
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("无法解析净值: {}", netValueStr);
        }
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private ParsedOldNetValue parseOldNetValueAndChangePercent(String oldNetValueStr, BigDecimal nav) {
        BigDecimal oldNetValue = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        BigDecimal changePercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        try {
            if (oldNetValueStr != null && !oldNetValueStr.trim().isEmpty()) {
                oldNetValue = new BigDecimal(oldNetValueStr).setScale(4, RoundingMode.HALF_UP);

                // 计算涨跌幅: (nav - oldNetValue) / oldNetValue * 100
                if (oldNetValue.compareTo(BigDecimal.ZERO) != 0) {
                    changePercent = nav.subtract(oldNetValue).divide(oldNetValue, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("无法解析昨日净值: {}", oldNetValueStr);
        }

        return new ParsedOldNetValue(oldNetValue, changePercent);
    }

    private QuoteDateTime parseQuoteDateTime(String timeStr) {
        LocalDate currentDate = LocalDate.now();
        LocalTime specificTime = LocalTime.now();

        // 先尝试解析 API 返回的时间
        if (timeStr != null && !timeStr.trim().isEmpty()) {
            specificTime = parseTime(timeStr);
        }

        // 用解析后的时间（或当前时间）判断是否在开盘时间段
        // 如果不在开盘时间段内，说明API返回的是上一个交易日的收盘数据，应该使用昨日日期
        if (!sinaFundApiClient.isMarketOpenTime(currentDate, specificTime)) {
            currentDate = currentDate.minusDays(1);
            log.info("当前非开盘时间段，使用昨日日期: {}", currentDate);
        }

        return new QuoteDateTime(currentDate, specificTime);
    }

    private LocalTime parseTime(String timeStr) {
        try {
            if (timeStr.contains(":")) {
                // 如果时间包含冒号，按标准时间格式解析
                return LocalTime.parse(timeStr.split("\\.")[0]);
            } else if (timeStr.length() == 6) {
                // HHMMSS 格式
                int hour = Integer.parseInt(timeStr.substring(0, 2));
                int minute = Integer.parseInt(timeStr.substring(2, 4));
                int second = Integer.parseInt(timeStr.substring(4, 6));
                return LocalTime.of(hour, minute, second);
            }
        } catch (Exception e) {
            log.warn("无法解析时间字符串: {}", timeStr);
        }
        return LocalTime.now();
    }

    private record ParsedOldNetValue(BigDecimal oldNetValue, BigDecimal changePercent) {
    }

    private record QuoteDateTime(LocalDate date, LocalTime time) {
    }
}
