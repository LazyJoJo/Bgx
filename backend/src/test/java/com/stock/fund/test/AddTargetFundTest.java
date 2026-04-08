package com.stock.fund.test;

import com.stock.fund.Application;
import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 添加目标基金功能测试类
 * 测试addTargetFund方法的各种场景
 */
@SpringBootTest
@ContextConfiguration(classes = Application.class)
@Transactional // 使用事务确保测试数据不会影响其他数据
class AddTargetFundTest {

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Test
    void testAddTargetFund_Success() {
        System.out.println("\n=== 测试添加基金目标 - 成功场景 ===");
        
        // 使用一个有效的基金代码进行测试
        String fundCode = "000001";
        System.out.println("尝试添加基金目标: " + fundCode);
        
        // 执行添加操作
        DataCollectionTarget target = dataCollectionAppService.addTargetFund(fundCode);
        
        // 验证结果
        assertNotNull(target, "目标对象不应为空");
        assertEquals(fundCode, target.getCode(), "基金代码应该匹配");
        assertNotNull(target.getName(), "基金名称不应为空");
        assertEquals("FUND", target.getType(), "类型应该是FUND");
        assertTrue(target.getActive(), "目标应该处于激活状态");
        assertEquals(Integer.valueOf(15), target.getCollectionFrequency(), "采集频率应该是15分钟");
        assertEquals("SINA_API", target.getDataSource(), "数据源应该是SINA_API");
        System.out.println("基金目标添加成功: " + target.getCode() + " - " + target.getName());
    }

    @Test
    void testAddTargetFund_Duplicate() {
        System.out.println("\n=== 测试添加基金目标 - 重复添加场景 ===");
        
        String fundCode = "000011"; // 使用另一个基金代码
        System.out.println("第一次添加基金目标: " + fundCode);
        
        // 第一次添加
        DataCollectionTarget firstTarget = dataCollectionAppService.addTargetFund(fundCode);
        assertNotNull(firstTarget, "第一次添加的目标对象不应为空");
        Long firstTargetId = firstTarget.getId();
        System.out.println("第一次添加成功，目标ID: " + firstTargetId);
        
        System.out.println("第二次添加相同基金目标: " + fundCode);
        // 第二次添加相同的基金代码，应该返回已存在的目标
        DataCollectionTarget secondTarget = dataCollectionAppService.addTargetFund(fundCode);
        assertNotNull(secondTarget, "第二次添加的目标对象不应为空");
        System.out.println("第二次调用返回，目标ID: " + secondTarget.getId());
        
        // 验证两次返回的是同一个目标
        assertEquals(firstTargetId, secondTarget.getId(), "两次调用应该返回同一个目标");
        assertEquals(firstTarget.getCode(), secondTarget.getCode(), "基金代码应该相同");
        assertEquals(firstTarget.getName(), secondTarget.getName(), "基金名称应该相同");
        System.out.println("重复添加测试通过，返回了已存在的目标配置");
    }

    @Test
    void testAddTargetFund_InvalidCode() {
        System.out.println("\n=== 测试添加基金目标 - 无效代码场景 ===");
        
        String invalidFundCode = "INVALID_CODE";
        System.out.println("尝试添加无效基金目标: " + invalidFundCode);
        
        // 应该抛出异常，因为无法获取到有效的基金数据
        assertThrows(RuntimeException.class, () -> {
            dataCollectionAppService.addTargetFund(invalidFundCode);
        }, "无效基金代码应该抛出异常");
        System.out.println("无效基金代码测试通过，正确抛出了异常");
    }

    @Test
    void testAddTargetFund_EmptyCode() {
        System.out.println("\n=== 测试添加基金目标 - 空代码场景 ===");
        
        String emptyFundCode = "";
        System.out.println("尝试添加空基金目标代码");
        
        // 应该抛出异常，因为无法获取到有效的基金数据
        assertThrows(RuntimeException.class, () -> {
            dataCollectionAppService.addTargetFund(emptyFundCode);
        }, "空基金代码应该抛出异常");
        System.out.println("空基金代码测试通过，正确抛出了异常");
    }

    @Test
    void testAddMultipleTargetFunds() {
        System.out.println("\n=== 测试添加多个基金目标 ===");
        
        String[] fundCodes = {"000001", "000011", "110011"};
        DataCollectionTarget[] targets = new DataCollectionTarget[fundCodes.length];
        
        // 添加多个基金目标
        for (int i = 0; i < fundCodes.length; i++) {
            System.out.println("添加第 " + (i + 1) + " 个基金目标: " + fundCodes[i]);
            targets[i] = dataCollectionAppService.addTargetFund(fundCodes[i]);
            assertNotNull(targets[i], "第 " + (i + 1) + " 个目标对象不应为空");
            assertEquals(fundCodes[i], targets[i].getCode(), "基金代码应该匹配");
            System.out.println("成功添加: " + targets[i].getCode() + " - " + targets[i].getName());
        }
        
        // 验证每个目标都是唯一的
        for (int i = 0; i < targets.length; i++) {
            for (int j = i + 1; j < targets.length; j++) {
                assertNotEquals(targets[i].getId(), targets[j].getId(), 
                    "不同的基金目标应该有不同的ID");
            }
        }
        System.out.println("多个基金目标添加测试通过");
    }
}