package com.stock.fund.interfaces.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.interfaces.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/data-collection-targets")
@Tag(name = "采集目标管理", description = "数据采集目标的增删改查和状态控制接口")
public class DataCollectionTargetController {

    @Autowired
    private DataCollectionTargetAppService dataCollectionTargetAppService;

    @PostMapping
    @Operation(summary = "创建采集目标", description = "新增一个数据采集目标")
    public ApiResponse<DataCollectionTarget> createTarget(@RequestBody DataCollectionTarget target) {
        DataCollectionTarget createdTarget = dataCollectionTargetAppService.createTarget(target);
        return ApiResponse.success("采集目标创建成功", createdTarget);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新采集目标 (按 ID)", description = "根据 ID 更新采集目标的信息")
    public ApiResponse<DataCollectionTarget> updateTarget(@Parameter(description = "采集目标 ID") @PathVariable Long id,
            @RequestBody DataCollectionTarget target) {
        DataCollectionTarget updatedTarget = dataCollectionTargetAppService.updateTarget(id, target);
        return ApiResponse.success("采集目标更新成功", updatedTarget);
    }

    @PutMapping("/code/{code}")
    @Operation(summary = "更新采集目标 (按代码)", description = "根据代码更新采集目标的信息")
    public ApiResponse<DataCollectionTarget> updateTargetByCode(
            @Parameter(description = "采集目标代码") @PathVariable String code, @RequestBody DataCollectionTarget target) {
        DataCollectionTarget updatedTarget = dataCollectionTargetAppService.updateTargetByCode(code, target);
        return ApiResponse.success("采集目标更新成功", updatedTarget);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采集目标 (按 ID)", description = "根据 ID 删除采集目标")
    public ApiResponse<String> deleteTarget(@PathVariable Long id) {
        dataCollectionTargetAppService.deleteTarget(id);
        return ApiResponse.success("采集目标删除成功");
    }

    @DeleteMapping("/code/{code}")
    @Operation(summary = "删除采集目标 (按代码)", description = "根据代码删除采集目标")
    public ApiResponse<String> deleteTargetByCode(@PathVariable String code) {
        dataCollectionTargetAppService.deleteTargetByCode(code);
        return ApiResponse.success("采集目标删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采集目标 (按 ID)", description = "根据 ID 查询采集目标详情")
    public ApiResponse<DataCollectionTarget> getTargetById(@PathVariable Long id) {
        DataCollectionTarget target = dataCollectionTargetAppService.getTargetById(id);
        return ApiResponse.success(target);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "查询采集目标 (按代码)", description = "根据代码查询采集目标详情")
    public ApiResponse<DataCollectionTarget> getTargetByCode(@PathVariable String code) {
        DataCollectionTarget target = dataCollectionTargetAppService.getTargetByCode(code);
        return ApiResponse.success(target);
    }

    @GetMapping
    @Operation(summary = "查询所有采集目标", description = "获取所有采集目标的列表")
    public ApiResponse<List<DataCollectionTarget>> getAllTargets() {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.getAllTargets();
        return ApiResponse.success(targets);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "按类型查询采集目标", description = "根据资产类型查询采集目标")
    public ApiResponse<List<DataCollectionTarget>> getTargetsByType(@PathVariable String type) {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsByType(type);
        return ApiResponse.success(targets);
    }

    @GetMapping("/active")
    @Operation(summary = "查询激活的采集目标", description = "获取所有处于激活状态的采集目标")
    public ApiResponse<List<DataCollectionTarget>> getActiveTargets() {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.getActiveTargets();
        return ApiResponse.success(targets);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类查询采集目标", description = "根据分类查询采集目标")
    public ApiResponse<List<DataCollectionTarget>> getTargetsByCategory(@PathVariable String category) {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsByCategory(category);
        return ApiResponse.success(targets);
    }

    @GetMapping("/needing-collection")
    @Operation(summary = "查询需要采集的目标", description = "获取超过采集间隔需要重新采集的目标列表")
    public ApiResponse<List<DataCollectionTarget>> getTargetsNeedingCollection() {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsNeedingCollection();
        return ApiResponse.success(targets);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索采集目标", description = "根据关键词搜索采集目标")
    public ApiResponse<List<DataCollectionTarget>> searchTargets(
            @Parameter(description = "标的类型") @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        List<DataCollectionTarget> targets = dataCollectionTargetAppService.searchTargets(type, keyword);
        return ApiResponse.success(targets);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "激活采集目标 (按 ID)", description = "将指定 ID 的采集目标设置为激活状态")
    public ApiResponse<String> activateTarget(@PathVariable Long id) {
        dataCollectionTargetAppService.activateTarget(id);
        return ApiResponse.success("采集目标已激活");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用采集目标 (按 ID)", description = "将指定 ID 的采集目标设置为停用状态")
    public ApiResponse<String> deactivateTarget(@PathVariable Long id) {
        dataCollectionTargetAppService.deactivateTarget(id);
        return ApiResponse.success("采集目标已停用");
    }

    @PostMapping("/code/{code}/activate")
    @Operation(summary = "激活采集目标 (按代码)", description = "将指定代码的采集目标设置为激活状态")
    public ApiResponse<String> activateTargetByCode(@PathVariable String code) {
        dataCollectionTargetAppService.activateTargetByCode(code);
        return ApiResponse.success("采集目标已激活");
    }

    @PostMapping("/code/{code}/deactivate")
    @Operation(summary = "停用采集目标 (按代码)", description = "将指定代码的采集目标设置为停用状态")
    public ApiResponse<String> deactivateTargetByCode(@PathVariable String code) {
        dataCollectionTargetAppService.deactivateTargetByCode(code);
        return ApiResponse.success("采集目标已停用");
    }

    @GetMapping("/count")
    @Operation(summary = "获取采集目标总数", description = "获取所有采集目标的总数")
    public ApiResponse<Long> getTargetCount() {
        long count = dataCollectionTargetAppService.getTargetCount();
        return ApiResponse.success(count);
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "按类型获取采集目标数量", description = "根据类型获取采集目标的统计数量")
    public ApiResponse<Long> getTargetCountByType(@PathVariable String type) {
        long count = dataCollectionTargetAppService.getTargetCountByType(type);
        return ApiResponse.success(count);
    }

    @GetMapping("/count/active")
    @Operation(summary = "获取激活采集目标数量", description = "获取所有处于激活状态的采集目标的统计数量")
    public ApiResponse<Long> getActiveTargetCount() {
        long count = dataCollectionTargetAppService.getActiveTargetCount();
        return ApiResponse.success(count);
    }
}
