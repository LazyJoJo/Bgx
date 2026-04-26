package com.stock.fund.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;

/**
 * 基于数据库中data_collection_target数据的测试类 从网络获取股票/基金数据并更新数据库中的名称
 */
@SpringBootTest
@ActiveProfiles("test")
// @Transactional // 使用事务确保测试数据不影响数据库
public class DataCollectionTargetUpdateTest {

    @Autowired
    private DataCollectionTargetRepository dataCollectionTargetRepository;

    /**
     * 测试从数据库读取采集目标并获取最新数据
     */
    @Test
    public void testDataCollectionAndNameUpdate() {
        System.out.println("开始测试：从数据库读取采集目标并更新名称...");

        // 从数据库获取所有活跃的采集目标
        List<DataCollectionTarget> activeTargets = dataCollectionTargetRepository.findActiveTargets();

        if (activeTargets.isEmpty()) {
            System.out.println("没有找到活跃的采集目标");
            return;
        }

        System.out.println("找到 " + activeTargets.size() + " 个活跃采集目标");

        for (DataCollectionTarget target : activeTargets) {

            System.out.println("正在处理目标: " + target.getCode() + " - " + target.getName());

            // 根据目标类型获取最新数据
            String latestName = fetchLatestName(target.getCode(), target.getType());

            if (latestName != null && !latestName.isEmpty()) {
                // 更新数据库中的名称
                String oldName = target.getName();
                target.setName(latestName);

                // 确保必要字段不为null
                if (target.getCollectionFrequency() == null) {
                    target.setCollectionFrequency(15); // 设置默认值
                }

                // 更新采集时间
                target.updateCollectionTime();

                // 确保更新时间也被设置
                target.setUpdatedAt(java.time.LocalDateTime.now());

                // 保存到数据库
                dataCollectionTargetRepository.save(target);

                System.out.println("成功更新: " + target.getCode() + " 名称从 '" + oldName + "' 更新为 '" + latestName + "'");
            } else {
                System.out.println("未能获取到 " + target.getCode() + " 的最新名称");
            }

        }

        System.out.println("测试完成");
    }

    /**
     * 获取最新的股票/基金名称
     * 
     * @param code 代码
     * @param type 类型 (STOCK/FUND)
     * @return 最新名称
     */
    private String fetchLatestName(String code, String type) {
        try {
            String urlStr;

            if ("FUND".equalsIgnoreCase(type)) {
                // 基金数据获取URL
                urlStr = String.format("https://hq.sinajs.cn/list=fu_%s", code);
            } else if ("STOCK".equalsIgnoreCase(type)) {
                // 股票数据获取URL (根据市场设置前缀)
                String marketPrefix = getMarketPrefix(code);
                urlStr = String.format("https://hq.sinajs.cn/list=%s%s", marketPrefix, code);
            } else {
                System.err.println("不支持的类型: " + type);
                return null;
            }

            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();

                // 设置请求头，伪装成浏览器，防止被反爬拦截
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                connection.setRequestProperty("Referer", "https://finance.sina.com.cn/");
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(10000); // 10秒读取超时

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("请求失败，HTTP Code: " + responseCode);
                    return null;
                }

                // 读取响应
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                // 解析并提取名称
                return extractNameFromResponse(content.toString(), type);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            System.err.println("获取 " + code + " 数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据代码判断市场前缀
     */
    private String getMarketPrefix(String code) {
        if (code.startsWith("6") || code.startsWith("5")) { // 6xx.xxx 上海A股/基金, 5xxx.xx 上海ETF
            return "sh";
        } else if (code.startsWith("0") || code.startsWith("1")) { // 0xx.xxx 深圳A股, 1xxx.xx 深圳ETF
            return "sz";
        } else if (code.startsWith("8")) { // 8xx.xxx 北交所
            return "bj";
        }
        return "sh"; // 默认上海市场
    }

    /**
     * 从响应中提取名称
     */
    private String extractNameFromResponse(String response, String type) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return null;
            }

            // 响应格式通常是 var hq_str_{code}="name,price,...";
            String[] lines = response.split(";");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("var hq_str_")) {
                    int start = line.indexOf("\"");
                    int end = line.lastIndexOf("\"");
                    if (start != -1 && end != -1 && start < end) {
                        String data = line.substring(start + 1, end);
                        String[] fields = data.split(",");

                        if (fields.length > 0) {
                            String name = fields[0].trim(); // 第一个字段通常是名称

                            // 如果名称包含不可见字符或长度异常，返回null
                            if (name != null && name.length() > 0 && name.length() < 50) {
                                return name;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析响应数据时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}