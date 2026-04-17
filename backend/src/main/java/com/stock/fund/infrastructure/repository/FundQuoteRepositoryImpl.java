package com.stock.fund.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.infrastructure.entity.FundQuotePO;
import com.stock.fund.infrastructure.mapper.FundQuoteMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FundQuoteRepositoryImpl implements FundQuoteRepository {

    private final FundQuoteMapper fundQuoteMapper;

    @Override
    public List<FundQuote> findByFundCode(String fundCode) {
        List<FundQuotePO> pos = fundQuoteMapper.findByFundCode(fundCode);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public FundQuote save(FundQuote fundQuote) {
        FundQuotePO existingPo = fundQuoteMapper.findByFundCodeAndQuoteDate(fundQuote.getFundCode(),
                fundQuote.getQuoteDate());

        FundQuotePO po = mapToPO(fundQuote);

        if (existingPo != null) {
            po.setId(existingPo.getId());
            po.setCreatedAt(existingPo.getCreatedAt());
            fundQuoteMapper.updateById(po);
            fundQuote.setId(po.getId());
        } else {
            fundQuoteMapper.insert(po);
            fundQuote.setId(po.getId());
        }
        return fundQuote;
    }

    @Override
    public List<FundQuote> saveAll(List<FundQuote> fundQuotes) {
        for (FundQuote fundQuote : fundQuotes) {
            save(fundQuote);
        }
        return fundQuotes;
    }

    @Override
    public FundQuote findLatestByFundCode(String fundCode) {
        FundQuotePO latestPo = fundQuoteMapper.findLatestByFundCode(fundCode);
        if (latestPo != null) {
            return mapToDomainEntity(latestPo);
        }
        return null;
    }

    @Override
    public FundQuote findByFundCodeAndQuoteDate(String fundCode, LocalDate quoteDate) {
        FundQuotePO po = fundQuoteMapper.findByFundCodeAndQuoteDate(fundCode, quoteDate);
        if (po != null) {
            return mapToDomainEntity(po);
        }
        return null;
    }

    @Override
    public List<FundQuote> findAllLatestQuotes() {
        List<FundQuotePO> pos = fundQuoteMapper.findAllLatestQuotes();
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public IPage<FundQuote> findPageByCondition(int pageNum, int pageSize, String fundCode, String fundName,
            LocalDate startDate, LocalDate endDate, String orderBy, String orderDirection) {
        Page<FundQuotePO> page = new Page<>(pageNum, pageSize);
        IPage<FundQuotePO> poPage = fundQuoteMapper.findPageByCondition(page, fundCode, fundName, startDate, endDate,
                orderBy, orderDirection);

        IPage<FundQuote> result = poPage.convert(this::mapToDomainEntity);
        return result;
    }

    private FundQuote mapToDomainEntity(FundQuotePO po) {
        FundQuote fundQuote = new FundQuote();
        fundQuote.setId(po.getId());
        fundQuote.setFundCode(po.getFundCode());
        fundQuote.setFundName(po.getFundName());
        fundQuote.setQuoteDate(po.getQuoteDate());
        fundQuote.setQuoteTimeOnly(po.getQuoteTimeOnly());

        fundQuote.setNav(po.getNav());
        fundQuote.setPrevNetValue(po.getPrevNetValue());
        fundQuote.setChangeAmount(po.getChangeAmount());
        fundQuote.setChangePercent(po.getChangePercent());
        fundQuote.setCreatedAt(po.getCreatedAt());
        return fundQuote;
    }

    private FundQuotePO mapToPO(FundQuote fundQuote) {
        FundQuotePO po = new FundQuotePO();
        po.setId(fundQuote.getId());
        po.setFundCode(fundQuote.getFundCode());
        po.setFundName(fundQuote.getFundName());
        po.setQuoteDate(fundQuote.getQuoteDate());
        po.setQuoteTimeOnly(fundQuote.getQuoteTimeOnly());

        po.setNav(fundQuote.getNav());
        po.setPrevNetValue(fundQuote.getPrevNetValue());
        po.setChangeAmount(fundQuote.getChangeAmount());
        po.setChangePercent(fundQuote.getChangePercent());
        po.setCreatedAt(fundQuote.getCreatedAt());
        return po;
    }
}
