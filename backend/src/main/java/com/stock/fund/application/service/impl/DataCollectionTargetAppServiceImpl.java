package com.stock.fund.application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.application.service.FundDataFetcher;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.exception.InvalidFundCodeException;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DataCollectionTargetAppServiceImpl implements DataCollectionTargetAppService {

    private final DataCollectionTargetRepository dataCollectionTargetRepository;
    private final FundDataFetcher fundDataFetcher;

    @Override
    public DataCollectionTarget createTargetByCode(String code) {
        // 防御性校验：防止空字符串或仅空白字符的代码
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("基金代码不能为空");
        }
        String trimmedCode = code.trim();

        // 如果已存在则直接返回（幂等性保障）
        var existing = dataCollectionTargetRepository.findByCode(trimmedCode);
        if (existing.isPresent()) {
            log.debug("Collection target {} already exists, returning directly", trimmedCode);
            return existing.get();
        }

        // 创建新的采集目标
        DataCollectionTarget target = new DataCollectionTarget();
        target.setCode(trimmedCode);
        target.setActive(true);

        // 使用独立的 FundDataFetcher Bean 获取基金信息（支持重试）
        // 注意：此端点仅支持 FUND 类型，STOCK 类型需使用其他接口
        var fundQuote = fundDataFetcher.fetchFundDataWithRetry(trimmedCode);

        if (fundQuote != null) {
            // 成功获取基金数据，确定为基金类型
            target.setType("FUND");
            target.setName(fundQuote.getFundName());
            log.info("Successfully obtained fund {} name: {}", trimmedCode, fundQuote.getFundName());
        } else {
            // 未能获取基金数据，拒绝创建（无效代码）
            log.error(
                    "Cannot create collection target {}: unable to obtain valid fund data, code may be invalid or a stock code (this interface only supports fund type)",
                    trimmedCode);
            throw new InvalidFundCodeException(trimmedCode, "无法获取有效的基金数据，请确认是否为有效的基金代码（此接口仅支持基金类型）");
        }

        return dataCollectionTargetRepository.save(target);
    }

    @Override
    public DataCollectionTarget updateTarget(Long id, DataCollectionTarget target) {
        DataCollectionTarget existingTarget = dataCollectionTargetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采集目标不存在: " + id));

        // 更新字段
        existingTarget.setName(target.getName());
        existingTarget.setType(target.getType());
        existingTarget.setMarket(target.getMarket());
        existingTarget.setActive(target.getActive());
        existingTarget.setCategory(target.getCategory());
        existingTarget.setDescription(target.getDescription());
        existingTarget.setCollectionFrequency(target.getCollectionFrequency());
        existingTarget.setDataSource(target.getDataSource());

        return dataCollectionTargetRepository.save(existingTarget);
    }

    @Override
    public DataCollectionTarget updateTargetByCode(String code, DataCollectionTarget target) {
        DataCollectionTarget existingTarget = dataCollectionTargetRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("采集目标代码不存在: " + code));

        return updateTarget(existingTarget.getId(), target);
    }

    @Override
    public void deleteTarget(Long id) {
        dataCollectionTargetRepository.deleteById(id);
    }

    @Override
    public void deleteTargetByCode(String code) {
        dataCollectionTargetRepository.deleteByCode(code);
    }

    @Override
    public DataCollectionTarget getTargetById(Long id) {
        return dataCollectionTargetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采集目标不存在: " + id));
    }

    @Override
    public DataCollectionTarget getTargetByCode(String code) {
        return dataCollectionTargetRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("采集目标代码不存在: " + code));
    }

    @Override
    public List<DataCollectionTarget> getAllTargets() {
        return dataCollectionTargetRepository.findByActive(null); // null表示查询所有
    }

    @Override
    public List<DataCollectionTarget> getTargetsByType(String type) {
        return dataCollectionTargetRepository.findByType(type);
    }

    @Override
    public List<DataCollectionTarget> getActiveTargets() {
        return dataCollectionTargetRepository.findActiveTargets();
    }

    @Override
    public List<DataCollectionTarget> getTargetsByCategory(String category) {
        return dataCollectionTargetRepository.findByCategory(category);
    }

    @Override
    public List<DataCollectionTarget> getTargetsNeedingCollection() {
        return dataCollectionTargetRepository.findTargetsNeedingCollection();
    }

    @Override
    public List<DataCollectionTarget> searchTargets(String type, String keyword) {
        if (type == null || type.isEmpty()) {
            return dataCollectionTargetRepository.search(keyword);
        }
        return dataCollectionTargetRepository.searchByType(type, keyword);
    }

    @Override
    public void activateTarget(Long id) {
        DataCollectionTarget target = getTargetById(id);
        target.activate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void deactivateTarget(Long id) {
        DataCollectionTarget target = getTargetById(id);
        target.deactivate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void activateTargetByCode(String code) {
        DataCollectionTarget target = getTargetByCode(code);
        target.activate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void deactivateTargetByCode(String code) {
        DataCollectionTarget target = getTargetByCode(code);
        target.deactivate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public long getTargetCount() {
        return dataCollectionTargetRepository.count();
    }

    @Override
    public long getTargetCountByType(String type) {
        return dataCollectionTargetRepository.countByType(type);
    }

    @Override
    public long getActiveTargetCount() {
        return dataCollectionTargetRepository.countByActive(true);
    }
}
