package com.decisionlense.backend.repository;

import com.decisionlense.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT COALESCE(SUM(s.revenue), 0) FROM Sale s")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Sale s")
    Long getTotalQuantity();

    @Query("""
        SELECT s.category, SUM(s.revenue)
        FROM Sale s
        GROUP BY s.category
    """)
    java.util.List<Object[]> getCategoryRevenue();
}