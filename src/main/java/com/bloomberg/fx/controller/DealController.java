package com.bloomberg.fx.controller;

import com.bloomberg.fx.dto.DealRequest;
import com.bloomberg.fx.mapper.DealMapper;
import com.bloomberg.fx.model.Deal;
import com.bloomberg.fx.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DealController {

    private final DealService dealService;
    private final DealMapper dealMapper;

    @PostMapping
    public ResponseEntity<String> importDeals(@RequestBody List<@Valid DealRequest> dealRequests) {
        log.info("Received request to import {} deals", dealRequests.size());

        int successCount = 0;
        int skipCount = 0;

        for (DealRequest dealRequest : dealRequests) {
            try {
                // Convert DTO to Entity using mapper
                Deal deal = dealMapper.toEntity(dealRequest);
                
                Deal savedDeal = dealService.saveDeal(deal);
                if (savedDeal != null) {
                    successCount++;
                } else {
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("Failed to save deal {}: {}", dealRequest.getDealUniqueId(), e.getMessage());
                // Continue processing other deals
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("Imported %d deals. Skipped %d duplicates/errors.", successCount, skipCount));
    }
}
