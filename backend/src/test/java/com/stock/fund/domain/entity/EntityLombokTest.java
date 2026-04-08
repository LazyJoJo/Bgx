package com.stock.fund.domain.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest

public class EntityLombokTest {

    @Test
    public void testStockEntityLombok() {
        Stock stock = new Stock();
        stock.setSymbol("000001");
        stock.setName("平安银行");
        stock.setIndustry("银行");
        stock.setMarket("深市");
        stock.setListingDate(LocalDate.of(1991, 4, 3));
        stock.setTotalShare(1000000.00);
        stock.setFloatShare(1000000.00);
        stock.setPe(5.23);
        stock.setPb(0.56);

        assertEquals("000001", stock.getSymbol());
        assertEquals("平安银行", stock.getName());
        assertEquals("银行", stock.getIndustry());
        assertEquals("深市", stock.getMarket());
        assertEquals(LocalDate.of(1991, 4, 3), stock.getListingDate());
        assertEquals(1000000.00, stock.getTotalShare());
        assertEquals(1000000.00, stock.getFloatShare());
        assertEquals(5.23, stock.getPe());
        assertEquals(0.56, stock.getPb());
    }

    @Test
    public void testStockQuoteEntityLombok() {
        StockQuote stockQuote = new StockQuote();
        stockQuote.setStockId(1L);
        stockQuote.setQuoteTime(LocalDateTime.now());
        stockQuote.setOpen(100.00);
        stockQuote.setHigh(105.00);
        stockQuote.setLow(98.00);
        stockQuote.setClose(102.50);
        stockQuote.setVolume(1000000L);
        stockQuote.setAmount(102500000.00);
        stockQuote.setChange(2.50);
        stockQuote.setChangePercent(2.50);

        assertEquals(1L, stockQuote.getStockId());
        assertNotNull(stockQuote.getQuoteTime());
        assertEquals(100.00, stockQuote.getOpen());
        assertEquals(105.00, stockQuote.getHigh());
        assertEquals(98.00, stockQuote.getLow());
        assertEquals(102.50, stockQuote.getClose());
        assertEquals(1000000L, stockQuote.getVolume());
        assertEquals(102500000.00, stockQuote.getAmount());
        assertEquals(2.50, stockQuote.getChange());
        assertEquals(2.50, stockQuote.getChangePercent());
    }

    @Test
    public void testFundEntityLombok() {
        Fund fund = new Fund();
        fund.setFundCode("000001");
        fund.setName("华夏成长混合");
        fund.setType("混合型");
        fund.setManager("王明");
        fund.setEstablishmentDate(LocalDate.of(2001, 12, 18));
        fund.setFundSize(50.00);
        fund.setNav(2.3567);
        fund.setDayGrowth(0.50);
        fund.setWeekGrowth(1.20);
        fund.setMonthGrowth(3.50);
        fund.setYearGrowth(15.80);

        assertEquals("000001", fund.getFundCode());
        assertEquals("华夏成长混合", fund.getName());
        assertEquals("混合型", fund.getType());
        assertEquals("王明", fund.getManager());
        assertEquals(LocalDate.of(2001, 12, 18), fund.getEstablishmentDate());
        assertEquals(50.00, fund.getFundSize());
        assertEquals(2.3567, fund.getNav());
        assertEquals(0.50, fund.getDayGrowth());
        assertEquals(1.20, fund.getWeekGrowth());
        assertEquals(3.50, fund.getMonthGrowth());
        assertEquals(15.80, fund.getYearGrowth());
    }
}