package com.bloomberg.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deal {

    @Id
    @Column(name = "deal_unique_id", nullable = false, unique = true)
    @NotNull(message = "Deal Unique Id is required")
    private String dealUniqueId;

    @Column(name = "ordering_currency", nullable = false, length = 3)
    @NotNull(message = "Ordering Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Ordering Currency must be a valid ISO 4217 code")
    private String orderingCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    @NotNull(message = "To Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "To Currency must be a valid ISO 4217 code")
    private String toCurrency;

    @Column(name = "deal_timestamp", nullable = false)
    @NotNull(message = "Deal Timestamp is required")
    private LocalDateTime dealTimestamp;

    @Column(name = "amount", nullable = false)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
}
