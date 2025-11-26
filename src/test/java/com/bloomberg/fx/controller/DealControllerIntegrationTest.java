package com.bloomberg.fx.controller;

import com.bloomberg.fx.dto.DealRequest;
import com.bloomberg.fx.model.Deal;
import com.bloomberg.fx.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DealControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterEach
    void tearDown() {
        dealRepository.deleteAll();
    }

    @Test
    void importDeals_ShouldSaveValidDealsAndSkipDuplicates() throws Exception {
        // Pre-insert deal1 to simulate duplicate
        Deal deal1Entity = Deal.builder()
                .dealUniqueId("d1")
                .orderingCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.now())
                .amount(BigDecimal.valueOf(100))
                .build();
        dealRepository.save(deal1Entity);

        // Use DTOs for API request
        DealRequest deal1 = DealRequest.builder()
                .dealUniqueId("d1")
                .orderingCurrency("USD")
                .toCurrency("EUR")
                .dealTimestamp(LocalDateTime.now())
                .amount(BigDecimal.valueOf(100))
                .build();

        DealRequest deal2 = DealRequest.builder()
                .dealUniqueId("d2")
                .orderingCurrency("GBP")
                .toCurrency("USD")
                .dealTimestamp(LocalDateTime.now())
                .amount(BigDecimal.valueOf(200))
                .build();

        List<DealRequest> deals = List.of(deal1, deal2);

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deals)))
                .andExpect(status().isCreated());

        assertEquals(2, dealRepository.count()); // d1 (existing) + d2 (new)
    }

    @Test
    void importDeals_ShouldValidateInput() throws Exception {
        DealRequest invalidDeal = DealRequest.builder()
                .dealUniqueId("d3")
                // Missing required fields
                .build();

        List<DealRequest> deals = List.of(invalidDeal);

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deals)))
                .andExpect(status().isBadRequest());
    }
}
