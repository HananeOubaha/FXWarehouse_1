package com.bloomberg.fx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Deal responses.
 * Returned by the API after successful import or queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealResponse {

    private String dealUniqueId;
    private String orderingCurrency;
    private String toCurrency;
    private LocalDateTime dealTimestamp;
    private BigDecimal amount;
}
