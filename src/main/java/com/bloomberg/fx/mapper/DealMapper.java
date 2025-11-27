package com.bloomberg.fx.mapper;

import com.bloomberg.fx.dto.DealRequest;
import com.bloomberg.fx.dto.DealResponse;
import com.bloomberg.fx.model.Deal;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between DTOs and Domain Entity.
 * Provides separation between API layer and persistence layer.
 */
@Component
public class DealMapper {

    /**
     * Converts DealRequest DTO to Deal entity.
     *
     * @param dealRequest The incoming DTO from API
     * @return Deal entity ready for persistence
     */
    public Deal toEntity(DealRequest dealRequest) {
        if (dealRequest == null) {
            return null;
        }

        return Deal.builder()
                .dealUniqueId(dealRequest.getDealUniqueId())
                .orderingCurrency(dealRequest.getOrderingCurrency())
                .toCurrency(dealRequest.getToCurrency())
                .dealTimestamp(dealRequest.getDealTimestamp())
                .amount(dealRequest.getAmount())
                .build();
    }

    /**
     * Converts Deal entity to DealResponse DTO.
     *
     * @param deal The domain entity
     * @return DTO for API response
     */
    public DealResponse toResponse(Deal deal) {
        if (deal == null) {
            return null;
        }

        return DealResponse.builder()
                .dealUniqueId(deal.getDealUniqueId())
                .orderingCurrency(deal.getOrderingCurrency())
                .toCurrency(deal.getToCurrency())
                .dealTimestamp(deal.getDealTimestamp())
                .amount(deal.getAmount())
                .build();
    }
}
