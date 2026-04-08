package com.stock.fund.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Hutool工具类使用示例
 *展如何正确使用Hutool替代自建工具类
 */
@Slf4j
@Component
public class HutoolDemo {
    
    @PostConstruct
    public void demonstrateHutoolUsage() {
        log.info("=== Hutool工具类使用示例 ===");
        
        demonstrateDateUtil();
        demonstrateJsonUtil();
        demonstrateStrUtil();
        demonstrateCollUtil();
        demonstrateSecureUtil();
        demonstrateHttpUtil();
        
        log.info("=== Hutool使用示例完成 ===");
    }
    
    private void demonstrateDateUtil() {
        log.info("--- 日期工具使用 (Hutool DateUtil) ---");
        
        // 当前时间
        String now = DateUtil.now();
        log.info("当前时间: {}", now);
        
        //格式化日期
        String formatted = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        log.info("格式化时间: {}", formatted);
        
        // 时间偏移
        Date futureDate = DateUtil.offsetDay(new Date(), 30);
        log.info("30天后: {}", DateUtil.format(futureDate, "yyyy-MM-dd"));
        
        // 时间计算
        long betweenDay = DateUtil.betweenDay(new Date(), futureDate, false);
        log.info("相差天数: {}天", betweenDay);
    }
    
    private void demonstrateJsonUtil() {
        log.info("--- JSON工具使用 (Hutool JSONUtil) ---");
        
        //测试数据
        Map<String, Object> data = new HashMap<>();
        data.put("name", "测试基金");
        data.put("code", "000001");
        data.put("nav", new BigDecimal("1.2345"));
        data.put("date", new Date());
        
        // 对象转JSON
        String json = JSONUtil.toJsonStr(data);
        log.info("对象转JSON: {}", json);
        
        // JSON转对象
        Map<String, Object> parsed = JSONUtil.toBean(json, Map.class);
        log.info("JSON转对象: {}", parsed);
        
        // JSON格式化
        String prettyJson = JSONUtil.toJsonPrettyStr(data);
        log.info("格式化JSON:\n{}", prettyJson);
    }
    
    private void demonstrateStrUtil() {
        log.info("--- 字符串工具使用 (Hutool StrUtil) ---");
        
        String email = "test@example.com";
        String phone = "13812345678";
        String emptyStr = "";
        String nullStr = null;
        
        // 字符串验证
        log.info("是否为空: {}", StrUtil.isEmpty(emptyStr));
        log.info("是否为空白: {}", StrUtil.isBlank(emptyStr));
        
        // 字符串处理
        log.info("安全转换null: '{}'", StrUtil.nullToEmpty(nullStr));
        log.info("安全转换默认值: '{}'", StrUtil.nullToDefault(nullStr, "default"));
        log.info("首字母大写: {}", StrUtil.upperFirst("hello"));
        log.info("字符串连接: {}", StrUtil.join(",", "a", "b", "c"));
    }
    
    private void demonstrateCollUtil() {
        log.info("---集合工具使用 (Hutool CollUtil) ---");
        
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("c", "d", "e");
        List<String> emptyList = new ArrayList<>();
        
        //验证
        log.info("集合是否为空: {}", CollUtil.isEmpty(emptyList));
        log.info("集合是否不为空: {}", CollUtil.isNotEmpty(list1));
        
        //操作
        log.info("第一个元素: {}", CollUtil.getFirst(list1));
        log.info("最后一个元素: {}", CollUtil.getLast(list1));
        
        //集合运算
        Collection<String> union = CollUtil.union(list1, list2);
        Collection<String> intersection = CollUtil.intersection(list1, list2);
        Collection<String> disjunction = CollUtil.disjunction(list1, list2);
        
        log.info("并集: {}", union);
        log.info("交集: {}", intersection);
        log.info("差集: {}", disjunction);
    }
    
    private void demonstrateSecureUtil() {
        log.info("--- 加密工具使用 (Hutool SecureUtil) ---");
        
        String text = "Hello World";
        String password = "myPassword123";
        
        //算法
        String md5 = SecureUtil.md5(text);
        String sha256 = SecureUtil.sha256(text);
        log.info("MD5: {}", md5);
        log.info("SHA256: {}", sha256);
        
        // Base64 (Hutool使用不同的方式)
        log.info("Hutool提供多种编码方式，请参考具体API文档");
        
        // UUID
        String uuid = UUID.randomUUID().toString();
        log.info("UUID生成: {}", uuid);
    }
    
    private void demonstrateHttpUtil() {
        log.info("--- HTTP工具使用 (Hutool HttpUtil) ---");
        
        // 注意：这里只是演示语法，实际使用时需要有效的URL
        log.info("GET请求示例: HttpUtil.get(\"https://api.example.com/data\")");
        log.info("POST请求示例: HttpUtil.post(\"https://api.example.com/data\", requestBody)");
        
        //检查URL可访问性示例
        String testUrl = "https://httpbin.org/get";
        log.info("测试URL: {}", testUrl);
        log.info("使用示例: String response = HttpUtil.get(\"{}\");", testUrl);
    }
    
    private void demonstrateOkHttp() {
        log.info("--- HTTP工具使用 (OkHttp3) ---");
        
        // OkHttp使用示例
        OkHttpClient client = new OkHttpClient();
        
        // GET请求示例
        String getUrl = "https://httpbin.org/get";
        Request getRequest = new Request.Builder()
                .url(getUrl)
                .build();
        log.info("GET请求示例: {}", getUrl);
        log.info("使用方式: OkHttpClient client = new OkHttpClient(); Request request = new Request.Builder().url(url).build();");
        
        // POST请求示例
        String postUrl = "https://httpbin.org/post";
        RequestBody requestBody = RequestBody.create("test data", MediaType.get("text/plain"));  
        Request postRequest = new Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .build();
        log.info("POST请求示例: {}", postUrl);
        log.info("支持JSON、表单、文件上传等多种请求体类型");
    }
    
    /**
     * 保留的业务特有工具类使用示例
     */
    private void demonstrateBusinessUtils() {
        log.info("--- 业务特有工具类使用示例 ---");
        
        // NumberUtils - 金融数据计算
        BigDecimal price1 = new BigDecimal("100.567");
        BigDecimal price2 = new BigDecimal("95.234");
        
        BigDecimal rounded = NumberUtils.round(price1, 2);
        BigDecimal percentChange = NumberUtils.calculatePercentChange(price1, price2);
        String currencyFormat = NumberUtils.formatCurrency(1234.56);
        
        log.info("金融数据四舍五入: {} -> {}", price1, rounded);
        log.info("百分比变化计算: {} -> {} = {}%", price2, price1, percentChange);
        log.info("货币格式化: {}", currencyFormat);
        
        // AssertUtils - 业务断言
        try {
            AssertUtils.notNull("test", "对象不能为空");
            AssertUtils.positive(new BigDecimal("100"), "数值必须为正数");
            log.info("业务断言通过");
        } catch (Exception e) {
            log.info("业务断言失败: {}", e.getMessage());
        }
        
        // ThreadPoolUtils -线程池管理
        String executorInfo = ThreadPoolUtils.getGlobalExecutorInfo();
        log.info("全局线程池信息: {}", executorInfo);
        
        // FileUtils - 文件操作
        boolean fileExists = FileUtils.exists("test.txt");
        log.info("文件是否存在: {}", fileExists);
    }
}