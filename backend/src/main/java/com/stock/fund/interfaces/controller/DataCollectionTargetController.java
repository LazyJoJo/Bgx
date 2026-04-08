package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/data-collection-targets")
@Tag(name = "采集目标管理", description = "数据采集目标的增删改查和状态控制接口")
public class DataCollectionTargetController {

    @Autowired
    private DataCollectionTargetAppService dataCollectionTargetAppService;
    
    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @PostMapping
    @Operation(summary = "创建采集目标", description = "新增一个数据采集目标，包括股票代码、基金代码等采集对象")
    public ApiResponse<DataCollectionTarget> createTarget(@RequestBody DataCollectionTarget target) {
        try {
            DataCollectionTarget createdTarget = dataCollectionTargetAppService.createTarget(target);
            return ApiResponse.success("采集目标创建成功", createdTarget);
        } catch (Exception e) {
            return ApiResponse.error("创建采集目标失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新采集目标 (按 ID)", description = "根据 ID 更新采集目标的信息")
    public ApiResponse<DataCollectionTarget> updateTarget(
            @Parameter(description = "采集目标 ID", example = "1") @PathVariable Long id, 
            @RequestBody DataCollectionTarget target) {
        try {
            DataCollectionTarget updatedTarget = dataCollectionTargetAppService.updateTarget(id, target);
            return ApiResponse.success("采集目标更新成功", updatedTarget);
        } catch (Exception e) {
            return ApiResponse.error("更新采集目标失败: " + e.getMessage());
        }
    }

    @PutMapping("/code/{code}")
    @Operation(summary = "更新采集目标 (按代码)", description = "根据代码更新采集目标的信息，适用于不知道 ID 的场景")
    public ApiResponse<DataCollectionTarget> updateTargetByCode(
            @Parameter(description = "采集目标代码", example = "SH600519") @PathVariable String code, 
            @RequestBody DataCollectionTarget target) {
        try {
            DataCollectionTarget updatedTarget = dataCollectionTargetAppService.updateTargetByCode(code, target);
            return ApiResponse.success("采集目标更新成功", updatedTarget);
        } catch (Exception e) {
            return ApiResponse.error("更新采集目标失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采集目标 (按 ID)", description = "根据 ID 删除采集目标")
    public ApiResponse<String> deleteTarget(@PathVariable Long id) {
        try {
            dataCollectionTargetAppService.deleteTarget(id);
            return ApiResponse.success("采集目标删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除采集目标失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/code/{code}")
    @Operation(summary = "删除采集目标 (按代码)", description = "根据代码删除采集目标")
    public ApiResponse<String> deleteTargetByCode(@PathVariable String code) {
        try {
            dataCollectionTargetAppService.deleteTargetByCode(code);
            return ApiResponse.success("采集目标删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除采集目标失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采集目标 (按 ID)", description = "根据 ID 查询采集目标详情")
    public ApiResponse<DataCollectionTarget> getTargetById(@PathVariable Long id) {
        try {
            DataCollectionTarget target = dataCollectionTargetAppService.getTargetById(id);
            return ApiResponse.success(target);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标失败: " + e.getMessage());
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "查询采集目标 (按代码)", description = "根据代码查询采集目标详情")
    public ApiResponse<DataCollectionTarget> getTargetByCode(@PathVariable String code) {
        try {
            DataCollectionTarget target = dataCollectionTargetAppService.getTargetByCode(code);
            return ApiResponse.success(target);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "查询所有采集目标", description = "获取所有采集目标的列表")
    public ApiResponse<List<DataCollectionTarget>> getAllTargets() {
        try {
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getAllTargets();
            return ApiResponse.success(targets);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "按类型查询采集目标", description = "根据资产类型查询采集目标，如 stock、fund 等")
    public ApiResponse<List<DataCollectionTarget>> getTargetsByType(@PathVariable String type) {
        try {
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsByType(type);
            return ApiResponse.success(targets);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @Operation(summary = "查询激活的采集目标", description = "获取所有处于激活状态的采集目标")
    public ApiResponse<List<DataCollectionTarget>> getActiveTargets() {
        try {
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getActiveTargets();
            return ApiResponse.success(targets);
        } catch (Exception e) {
            return ApiResponse.error("获取激活采集目标列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类查询采集目标", description = "根据分类查询采集目标")
    public ApiResponse<List<DataCollectionTarget>> getTargetsByCategory(@PathVariable String category) {
        try {
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsByCategory(category);
            return ApiResponse.success(targets);
        } catch (Exception e) {
            return ApiResponse.error("获取分类采集目标列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/needing-collection")
    @Operation(summary = "查询需要采集的目标", description = "获取超过采集间隔需要重新采集的目标列表")
    public ApiResponse<List<DataCollectionTarget>> getTargetsNeedingCollection() {
        try {
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsNeedingCollection();
            return ApiResponse.success(targets);
        } catch (Exception e) {
            return ApiResponse.error("获取需要采集的目标列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "激活采集目标 (按 ID)", description = "将指定 ID 的采集目标设置为激活状态")
    public ApiResponse<String> activateTarget(@PathVariable Long id) {
        try {
            dataCollectionTargetAppService.activateTarget(id);
            return ApiResponse.success("采集目标已激活");
        } catch (Exception e) {
            return ApiResponse.error("激活采集目标失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用采集目标 (按 ID)", description = "将指定 ID 的采集目标设置为停用状态")
    public ApiResponse<String> deactivateTarget(@PathVariable Long id) {
        try {
            dataCollectionTargetAppService.deactivateTarget(id);
            return ApiResponse.success("采集目标已停用");
        } catch (Exception e) {
            return ApiResponse.error("停用采集目标失败: " + e.getMessage());
        }
    }

    @PostMapping("/code/{code}/activate")
    @Operation(summary = "激活采集目标 (按代码)", description = "将指定代码的采集目标设置为激活状态")
    public ApiResponse<String> activateTargetByCode(@PathVariable String code) {
        try {
            dataCollectionTargetAppService.activateTargetByCode(code);
            return ApiResponse.success("采集目标已激活");
        } catch (Exception e) {
            return ApiResponse.error("激活采集目标失败: " + e.getMessage());
        }
    }

    @PostMapping("/code/{code}/deactivate")
    @Operation(summary = "停用采集目标 (按代码)", description = "将指定代码的采集目标设置为停用状态")
    public ApiResponse<String> deactivateTargetByCode(@PathVariable String code) {
        try {
            dataCollectionTargetAppService.deactivateTargetByCode(code);
            return ApiResponse.success("采集目标已停用");
        } catch (Exception e) {
            return ApiResponse.error("停用采集目标失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @Operation(summary = "统计采集目标总数", description = "获取所有采集目标的数量")
    public ApiResponse<Long> getTargetCount() {
        try {
            long count = dataCollectionTargetAppService.getTargetCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "按类型统计数量", description = "统计指定类型的采集目标数量")
    public ApiResponse<Long> getTargetCountByType(@PathVariable String type) {
        try {
            long count = dataCollectionTargetAppService.getTargetCountByType(type);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取采集目标数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/count/active")
    @Operation(summary = "统计激活目标数量", description = "统计处于激活状态的采集目标数量")
    public ApiResponse<Long> getActiveTargetCount() {
        try {
            long count = dataCollectionTargetAppService.getActiveTargetCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取激活采集目标数量失败: " + e.getMessage());
        }
    }

    @PostMapping("/add-fund")
    @Operation(summary = "添加基金采集目标", description = "快速添加一个基金到采集目标列表，自动设置默认配置")
    public ApiResponse<DataCollectionTarget> addFundTarget(
            @Parameter(description = "基金代码", example = "000001") @RequestParam String fundCode) {
        try {
            DataCollectionTarget target = dataCollectionAppService.addTargetFund(fundCode);
            return ApiResponse.success("基金目标添加成功", target);
        } catch (Exception e) {
            return ApiResponse.error("添加基金目标失败：" + e.getMessage());
        }
    }
}