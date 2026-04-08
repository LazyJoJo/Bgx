package com.stock.fund.test;

import com.stock.fund.Application;
import com.stock.fund.application.service.DataCollectionAppService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * 数据库驱动的数据获取测试应用
 * 用于测试正式代码片段
 */
@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class DatabaseDrivenDataFetcherApp  {

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Test
    public void test2() {
        // System.out.println("开始测试基金实时数据获取功能...");
        dataCollectionAppService.fetchAndSaveFundRealTimeData();
        // System.out.println("基金实时数据获取测试完成");
        
        // System.out.println("\n开始测试基金目标添加功能...");
        // testAddTargetFund();
        // System.out.println("基金目标添加功能测试完成");
        
    }
    
    /**
     * 测试添加目标基金功能
     */
    void testAddTargetFund() {
        try {
            // 测试添加基金目标
            System.out.println("\n=== 测试添加基金目标 ===");
            
            // 测试添加一个基金目标
            String fundCode = "012042";  // 示例基金代码
            System.out.println("尝试添加基金目标: " + fundCode);
            
            var target = dataCollectionAppService.addTargetFund(fundCode);
            System.out.println("基金目标添加成功: " + target.getCode() + " - " + target.getName());
            System.out.println("目标ID: " + target.getId());
            System.out.println("目标类型: " + target.getType());
            System.out.println("是否激活: " + target.getActive());
            
            System.out.println("\n=== 测试重复添加相同基金目标 ===");
            // 再次尝试添加相同的基金目标，应该返回已存在的配置
            var existingTarget = dataCollectionAppService.addTargetFund(fundCode);
            System.out.println("基金目标已存在，返回已有的配置: " + existingTarget.getCode() + " - " + existingTarget.getName());
            System.out.println("目标ID: " + existingTarget.getId());
            System.out.println("是否为同一对象: " + (target.getId().equals(existingTarget.getId())));
            
            
            
        } catch (Exception e) {
            System.err.println("测试基金目标添加功能时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}