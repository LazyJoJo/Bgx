package com.stock.fund.infrastructure.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 新浪基金 API 客户端
 * 
 * 将外部 API 调用逻辑从应用服务中分离出来， 遵循 DDD 基础设施层的职责划分。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SinaFundApiClient {

    private final OkHttpClient httpClient;

    /**
     * 从新浪 API 获取基金实时数据
     * 
     * @param fundCode 基金代码
     * @return 基金数据 Map，包含 name, netValue, oldNetValue, time 等字段
     */
    public Map<String, String> fetchFundData(String fundCode) {
        String urlStr = String.format("https://hq.sinajs.cn/list=fu_%s", fundCode);

        Request request = new Request.Builder().url(urlStr).addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://finance.sina.com.cn/").build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("请求基金数据失败，HTTP Code: {}", response.code());
                return null;
            }

            ResponseBody body = response.body();
            if (body == null) {
                log.warn("响应体为空");
                return null;
            }

            // 获取原始字节数组以处理可能的编码问题
            byte[] bytes = body.bytes();
            // 根据新浪API特性，使用GBK编码处理中文
            String content = new String(bytes, java.nio.charset.Charset.forName("GBK"));

            // 解析基金数据
            return parseFundData(content, fundCode);

        } catch (Exception e) {
            log.error("获取基金 {} 数据时发生异常", fundCode, e);
            return null;
        }
    }

    /**
     * 解析新浪基金数据响应
     * 
     * @param response 响应数据
     * @param fundCode 基金代码
     * @return 解析后的基金数据 Map
     */
    private Map<String, String> parseFundData(String response, String fundCode) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return null;
            }

            // 响应格式通常是 var
            // hq_str_fu_{code}="name,date,time,net_value,growth_rate,other_data";
            String[] lines = response.split(";");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("var hq_str_")) {
                    int start = line.indexOf('"');
                    int end = line.lastIndexOf('"');
                    if (start != -1 && end != -1 && start < end) {
                        String data = line.substring(start + 1, end);
                        String[] fields = data.split(",");

                        if (fields.length >= 5) { // 至少需要基金名称、时间、净值、昨日净值
                            Map<String, String> fundData = new HashMap<>();
                            fundData.put("fundCode", fundCode);
                            fundData.put("name", fields[0]); // 基金名称
                            fundData.put("time", fields[1]); // 时间
                            fundData.put("netValue", fields[2]); // 净值
                            fundData.put("oldNetValue", fields[3]); // 昨日净值
                            return fundData;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析基金数据时发生异常", e);
        }

        return null;
    }

    /**
     * 将基金数据转换为 BigDecimal 净值
     */
    public BigDecimal parseNetValue(String netValueStr) {
        if (netValueStr == null || netValueStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(netValueStr).setScale(4, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("无法解析净值: {}", netValueStr);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 判断当前是否在开盘时间段
     */
    public boolean isMarketOpenTime(LocalDate date, LocalTime time) {
        // 检查是否为周末
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime marketClose = LocalTime.of(15, 0);
        return !time.isBefore(marketOpen) && !time.isAfter(marketClose);
    }
}
