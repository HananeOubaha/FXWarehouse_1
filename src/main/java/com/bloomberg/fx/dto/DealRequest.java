package com.bloomberg.fx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for incoming Deal import requests.
 * Separates API contract from the domain entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealRequest {

    @NotNull(message = "Deal Unique Id is required")
    private String dealUniqueId;

    @NotNull(message = "Ordering Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Ordering Currency must be a valid ISO 4217 code")
    private String orderingCurrency;

    @NotNull(message = "To Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "To Currency must be a valid ISO 4217 code")
    private String toCurrency;

    @NotNull(message = "Deal Timestamp is required")
    private LocalDateTime dealTimestamp;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
}
