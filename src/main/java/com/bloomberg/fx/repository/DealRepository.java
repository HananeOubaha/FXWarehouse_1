package com.bloomberg.fx.repository;

import com.bloomberg.fx.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {
    boolean existsByDealUniqueId(String dealUniqueId);
}
