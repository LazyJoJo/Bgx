package com.stock.fund.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")

public class EntityLombokTest {

    @Test
    public void testStockEntityLombok() {
        Stock stock = new Stock();
        stock.setSymbol("000001");
        stock.setName("平安银行");
        stock.setIndustry("银行");
        stock.setMarket("深市");
        stock.setListingDate(LocalDate.of(1991, 4, 3));
        stock.setTotalShare(new BigDecimal("1000000.00"));
        stock.setFloatShare(new BigDecimal("1000000.00"));
        stock.setPe(new BigDecimal("5.23"));
        stock.setPb(new BigDecimal("0.56"));

        assertEquals("000001", stock.getSymbol());
        assertEquals("平安银行", stock.getName());
        assertEquals("银行", stock.getIndustry());
        assertEquals("深市", stock.getMarket());
        assertEquals(LocalDate.of(1991, 4, 3), stock.getListingDate());
        assertEquals(new BigDecimal("1000000.00"), stock.getTotalShare());
        assertEquals(new BigDecimal("1000000.00"), stock.getFloatShare());
        assertEquals(new BigDecimal("5.23"), stock.getPe());
        assertEquals(new BigDecimal("0.56"), stock.getPb());
    }

    @Test
    public void testStockQuoteEntityLombok() {
        StockQuote stockQuote = new StockQuote();
        stockQuote.setStockId(1L);
        stockQuote.setQuoteTime(LocalDateTime.now());
        stockQuote.setOpen(new BigDecimal("100.00"));
        stockQuote.setHigh(new BigDecimal("105.00"));
        stockQuote.setLow(new BigDecimal("98.00"));
        stockQuote.setClose(new BigDecimal("102.50"));
        stockQuote.setVolume(1000000L);
        stockQuote.setAmount(new BigDecimal("102500000.00"));
        stockQuote.setChange(new BigDecimal("2.50"));
        stockQuote.setChangePercent(new BigDecimal("2.50"));

        assertEquals(1L, stockQuote.getStockId());
        assertNotNull(stockQuote.getQuoteTime());
        assertEquals(new BigDecimal("100.00"), stockQuote.getOpen());
        assertEquals(new BigDecimal("105.00"), stockQuote.getHigh());
        assertEquals(new BigDecimal("98.00"), stockQuote.getLow());
        assertEquals(new BigDecimal("102.50"), stockQuote.getClose());
        assertEquals(1000000L, stockQuote.getVolume());
        assertEquals(new BigDecimal("102500000.00"), stockQuote.getAmount());
        assertEquals(new BigDecimal("2.50"), stockQuote.getChange());
        assertEquals(new BigDecimal("2.50"), stockQuote.getChangePercent());
    }

    @Test
    public void testFundEntityLombok() {
        Fund fund = new Fund();
        fund.setFundCode("000001");
        fund.setName("华夏成长混合");
        fund.setType("混合型");
        fund.setManager("王明");
        fund.setEstablishmentDate(LocalDate.of(2001, 12, 18));
        fund.setFundSize(new BigDecimal("50.00"));
        fund.setNav(new BigDecimal("2.3567"));
        fund.setDayGrowth(new BigDecimal("0.50"));
        fund.setWeekGrowth(new BigDecimal("1.20"));
        fund.setMonthGrowth(new BigDecimal("3.50"));
        fund.setYearGrowth(new BigDecimal("15.80"));

        assertEquals("000001", fund.getFundCode());
        assertEquals("华夏成长混合", fund.getName());
        assertEquals("混合型", fund.getType());
        assertEquals("王明", fund.getManager());
        assertEquals(LocalDate.of(2001, 12, 18), fund.getEstablishmentDate());
        assertEquals(new BigDecimal("50.00"), fund.getFundSize());
        assertEquals(new BigDecimal("2.3567"), fund.getNav());
        assertEquals(new BigDecimal("0.50"), fund.getDayGrowth());
        assertEquals(new BigDecimal("1.20"), fund.getWeekGrowth());
        assertEquals(new BigDecimal("3.50"), fund.getMonthGrowth());
        assertEquals(new BigDecimal("15.80"), fund.getYearGrowth());
    }
}
