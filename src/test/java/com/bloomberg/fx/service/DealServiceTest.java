package com.bloomberg.fx.service;

import com.bloomberg.fx.model.Deal;
import com.bloomberg.fx.repository.DealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealService dealService;

    @Test
    void saveDeal_ShouldSaveDeal_WhenDealIsNew() {
        Deal deal = Deal.builder()
                .dealUniqueId("deal1")
                .orderingCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.now())
                .amount(BigDecimal.valueOf(100.0))
                .build();

        when(dealRepository.existsByDealUniqueId("deal1")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenReturn(deal);

        Deal savedDeal = dealService.saveDeal(deal);

        assertNotNull(savedDeal);
        assertEquals("deal1", savedDeal.getDealUniqueId());
        verify(dealRepository, times(1)).save(deal);
    }

    @Test
    void saveDeal_ShouldReturnNull_WhenDealAlreadyExists() {
        Deal deal = Deal.builder()
                .dealUniqueId("deal1")
                .build();

        when(dealRepository.existsByDealUniqueId("deal1")).thenReturn(true);

        Deal savedDeal = dealService.saveDeal(deal);

        assertNull(savedDeal);
        verify(dealRepository, never()).save(any(Deal.class));
    }
}
