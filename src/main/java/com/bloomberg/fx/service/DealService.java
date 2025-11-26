package com.bloomberg.fx.service;

import com.bloomberg.fx.model.Deal;
import com.bloomberg.fx.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final DealRepository dealRepository;

    /**
     * Saves a deal to the database.
     * Uses REQUIRES_NEW propagation to ensure that each deal is processed in its
     * own transaction.
     * This allows valid deals to be saved even if others in the same batch fail.
     *
     * @param deal The deal to save
     * @return The saved deal, or null if it already exists
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Deal saveDeal(Deal deal) {
        if (dealRepository.existsByDealUniqueId(deal.getDealUniqueId())) {
            log.warn("Deal with ID {} already exists. Skipping import.", deal.getDealUniqueId());
            return null; // Or throw a specific exception if you want to signal duplication explicitly
        }
        log.info("Saving deal: {}", deal.getDealUniqueId());
        return dealRepository.save(deal);
    }
}
