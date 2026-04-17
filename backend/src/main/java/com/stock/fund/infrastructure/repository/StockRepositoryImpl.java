package com.stock.fund.infrastructure.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.infrastructure.entity.StockBasicPO;
import com.stock.fund.infrastructure.mapper.StockBasicMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final StockBasicMapper stockBasicMapper;

    @Override
    public Optional<Stock> findBySymbol(String symbol) {
        StockBasicPO po = stockBasicMapper.findBySymbol(symbol);
        if (po != null) {
            return Optional.of(mapToDomainEntity(po));
        }
        return Optional.empty();
    }

    @Override
    public List<Stock> findAll() {
        List<StockBasicPO> pos = stockBasicMapper.selectList(null);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public Stock save(Stock stock) {
        StockBasicPO po = mapToPO(stock);
        if (stock.getId() == null) {
            stockBasicMapper.insert(po);
            stock.setId(po.getId());
        } else {
            stockBasicMapper.updateById(po);
        }
        return stock;
    }

    @Override
    public void deleteById(Long id) {
        stockBasicMapper.deleteById(id);
    }

    @Override
    public List<Stock> findByIndustry(String industry) {
        List<StockBasicPO> pos = stockBasicMapper.findByIndustry(industry);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<Stock> findByMarket(String market) {
        List<StockBasicPO> pos = stockBasicMapper.findByMarket(market);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public Map<String, Stock> findBySymbols(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Map.of();
        }
        List<StockBasicPO> pos = stockBasicMapper.findBySymbols(symbols);
        return pos.stream().map(this::mapToDomainEntity)
                .collect(Collectors.toMap(Stock::getSymbol, Function.identity()));
    }

    private Stock mapToDomainEntity(StockBasicPO po) {
        Stock stock = new Stock();
        stock.setId(po.getId());
        stock.setSymbol(po.getSymbol());
        stock.setName(po.getName());
        stock.setIndustry(po.getIndustry());
        stock.setMarket(po.getMarket());
        stock.setListingDate(po.getListingDate());
        stock.setTotalShare(po.getTotalShare());
        stock.setFloatShare(po.getFloatShare());
        stock.setPe(po.getPe());
        stock.setPb(po.getPb());
        stock.setCreatedAt(po.getCreatedAt());
        stock.setUpdatedAt(po.getUpdatedAt());
        return stock;
    }

    private StockBasicPO mapToPO(Stock stock) {
        StockBasicPO po = new StockBasicPO();
        po.setId(stock.getId());
        po.setSymbol(stock.getSymbol());
        po.setName(stock.getName());
        po.setIndustry(stock.getIndustry());
        po.setMarket(stock.getMarket());
        po.setListingDate(stock.getListingDate());
        po.setTotalShare(stock.getTotalShare());
        po.setFloatShare(stock.getFloatShare());
        po.setPe(stock.getPe());
        po.setPb(stock.getPb());
        po.setCreatedAt(stock.getCreatedAt());
        po.setUpdatedAt(stock.getUpdatedAt());
        return po;
    }
}