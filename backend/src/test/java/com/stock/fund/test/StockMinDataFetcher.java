package com.stock.fund.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class StockMinDataFetcher {

    /**
     * 获取A股实时分钟数据
     * @param symbol 股票代码 (6位数字，如 "000001")
     * @param period 周期 (1, 5, 15, 30, 60)
     */
    public static void fetchMinData(String code) {
        String urlStr = String.format(
                "https://finance.sina.com.cn/fund/quotes/%s/bc.shtml",
                code
        );

        String listUrlStr = String.format(
                "https://hq.sinajs.cn/list=fu_%s",
                code
        );

        HttpURLConnection connection = null;
        try {
            URL url = new URL(listUrlStr);
            connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求头，伪装成浏览器，防止被反爬拦截
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Referer", "https://finance.sina.com.cn/");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("请求失败，HTTP Code: " + responseCode);
            }



            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            Map<String, String> funcData = parseFuncData(content.toString());

            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 解析 JSON 数据
     * funcName : 基金名称
     * date: 基金数据最后采集的时间
     * funcNum: 昨天收盘价格
     * funcNewNum: 最新实时价格
     * 
     */
    private static Map<String, String> parseFuncData(String json) {
        
        String[] str = json.split(",");

        String funcName = str[0];
        String date = str[1];
        String funcNewNum = str[2];
        String funcNum = str[3];

        Map<String, String> map = new HashMap<>();
        map.put("funcName", funcName);
        map.put("date", date);
        map.put("funcNum", funcNum);
        map.put("funcNewNum", funcNewNum);

        return map;

    }


    /**
     * 简易解析 JSON 并打印最后几条数据
     */
    private static void parseAndPrintData(String json, String code) {
        // 这是一个非常简陋的字符串解析，仅为了演示不依赖第三方库
        // 实际开发请使用 Jackson, Gson 或 FastJSON
        
        // 1. 找到股票名称
        String funcNameStr = "class=\"fund_name\">";
        int nameIndex = json.indexOf(funcNameStr);
        String funcName = "未知";
        if (nameIndex != -1) {
            int start = nameIndex + funcNameStr.length();
            int end = json.indexOf("</h1>", start);
            funcName = json.substring(start, end);
        }

        // 2. 获取原先值
        String fundDataStr = "\"fund_data\">";
        double funcNum = 0;
        int dataIndex = json.indexOf(fundDataStr);
        if (dataIndex != -1) {
            int start = dataIndex + fundDataStr.length();
            int end = json.indexOf("</span>", start);
            String num = json.substring(start, end);
            funcNum = Double.parseDouble(num);
        }
        
        
        // 3、获取最新值
        String fundNewDataStr = "\"zxgz_data_down\">";
        double funcNewNum = 0;
        int newDataIndex = json.indexOf(fundNewDataStr);
        if (newDataIndex != -1) {
            int start = newDataIndex + fundNewDataStr.length();
            int end = json.indexOf("</span>", start);
            String num = json.substring(start, end);
            if ("--".equalsIgnoreCase(num)){
                funcNewNum = Double.parseDouble(num);
            }
        }

        System.out.println(funcName + ": " + funcNum+" - "+funcNewNum);

    }

    public static void main(String[] args) {
        fetchMinData("100053");
    }
}
