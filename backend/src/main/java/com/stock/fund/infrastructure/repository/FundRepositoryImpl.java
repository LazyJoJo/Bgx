package com.stock.fund.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.infrastructure.entity.FundBasicPO;
import com.stock.fund.infrastructure.mapper.FundBasicMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FundRepositoryImpl implements FundRepository {

    private final FundBasicMapper fundBasicMapper;

    @Override
    public Optional<Fund> findByFundCode(String fundCode) {
        FundBasicPO po = fundBasicMapper.findByFundCode(fundCode);
        if (po != null) {
            return Optional.of(mapToDomainEntity(po));
        }
        return Optional.empty();
    }

    @Override
    public List<Fund> findAll() {
        List<FundBasicPO> pos = fundBasicMapper.selectList(null);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public Fund save(Fund fund) {
        FundBasicPO po = mapToPO(fund);
        if (fund.getId() == null) {
            fundBasicMapper.insert(po);
            fund.setId(po.getId());
        } else {
            fundBasicMapper.updateById(po);
        }
        return fund;
    }

    @Override
    public void deleteById(Long id) {
        fundBasicMapper.deleteById(id);
    }

    @Override
    public List<Fund> findByType(String type) {
        List<FundBasicPO> pos = fundBasicMapper.findByType(type);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Fund> findByManager(String manager) {
        List<FundBasicPO> pos = fundBasicMapper.findByManager(manager);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    private Fund mapToDomainEntity(FundBasicPO po) {
        Fund fund = new Fund();
        fund.setId(po.getId());
        fund.setFundCode(po.getFundCode());
        fund.setName(po.getName());
        fund.setType(po.getType());
        fund.setManager(po.getManager());
        fund.setEstablishmentDate(po.getEstablishmentDate());
        fund.setFundSize(po.getFundSize());
        fund.setNav(po.getNav());
        fund.setDayGrowth(po.getDayGrowth());
        fund.setWeekGrowth(po.getWeekGrowth());
        fund.setMonthGrowth(po.getMonthGrowth());
        fund.setYearGrowth(po.getYearGrowth());
        fund.setCreatedAt(po.getCreatedAt());
        fund.setUpdatedAt(po.getUpdatedAt());
        return fund;
    }

    private FundBasicPO mapToPO(Fund fund) {
        FundBasicPO po = new FundBasicPO();
        po.setId(fund.getId());
        po.setFundCode(fund.getFundCode());
        po.setName(fund.getName());
        po.setType(fund.getType());
        po.setManager(fund.getManager());
        po.setEstablishmentDate(fund.getEstablishmentDate());
        po.setFundSize(fund.getFundSize());
        po.setNav(fund.getNav());
        po.setDayGrowth(fund.getDayGrowth());
        po.setWeekGrowth(fund.getWeekGrowth());
        po.setMonthGrowth(fund.getMonthGrowth());
        po.setYearGrowth(fund.getYearGrowth());
        po.setCreatedAt(fund.getCreatedAt());
        po.setUpdatedAt(fund.getUpdatedAt());
        return po;
    }
}