package com.stock.fund.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.infrastructure.entity.StockQuotePO;
import com.stock.fund.infrastructure.mapper.StockQuoteMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockQuoteRepositoryImpl implements StockQuoteRepository {

    private final StockQuoteMapper stockQuoteMapper;

    @Override
    public List<StockQuote> findByStockId(Long stockId) {
        List<StockQuotePO> pos = stockQuoteMapper.findByStockId(stockId);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<StockQuote> findByStockIdAndQuoteTimeBetween(Long stockId, LocalDateTime start, LocalDateTime end) {
        List<StockQuotePO> pos = stockQuoteMapper.findByStockIdAndQuoteTimeBetween(stockId, start, end);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public StockQuote save(StockQuote stockQuote) {
        StockQuotePO po = mapToPO(stockQuote);
        if (stockQuote.getId() == null) {
            stockQuoteMapper.insert(po);
            stockQuote.setId(po.getId());
        } else {
            stockQuoteMapper.updateById(po);
        }
        return stockQuote;
    }

    @Override
    public List<StockQuote> saveAll(List<StockQuote> stockQuotes) {
        for (StockQuote stockQuote : stockQuotes) {
            save(stockQuote);
        }
        return stockQuotes;
    }

    @Override
    public List<StockQuote> findAllLatestQuotes() {
        List<StockQuotePO> pos = stockQuoteMapper.findAllLatestQuotes();
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    private StockQuote mapToDomainEntity(StockQuotePO po) {
        StockQuote stockQuote = new StockQuote();
        stockQuote.setId(po.getId());
        stockQuote.setStockId(po.getStockId());
        stockQuote.setQuoteTime(po.getQuoteTime());
        stockQuote.setOpen(po.getOpen());
        stockQuote.setHigh(po.getHigh());
        stockQuote.setLow(po.getLow());
        stockQuote.setClose(po.getClose());
        stockQuote.setVolume(po.getVolume());
        stockQuote.setAmount(po.getAmount());
        stockQuote.setChange(po.getChange());
        stockQuote.setChangePercent(po.getChangePercent());
        stockQuote.setCreatedAt(po.getCreatedAt());
        return stockQuote;
    }

    private StockQuotePO mapToPO(StockQuote stockQuote) {
        StockQuotePO po = new StockQuotePO();
        po.setId(stockQuote.getId());
        po.setStockId(stockQuote.getStockId());
        po.setQuoteTime(stockQuote.getQuoteTime());
        po.setOpen(stockQuote.getOpen());
        po.setHigh(stockQuote.getHigh());
        po.setLow(stockQuote.getLow());
        po.setClose(stockQuote.getClose());
        po.setVolume(stockQuote.getVolume());
        po.setAmount(stockQuote.getAmount());
        po.setChange(stockQuote.getChange());
        po.setChangePercent(stockQuote.getChangePercent());
        po.setCreatedAt(stockQuote.getCreatedAt());
        return po;
    }
}