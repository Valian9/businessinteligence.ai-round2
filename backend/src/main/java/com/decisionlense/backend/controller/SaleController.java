package com.decisionlense.backend.controller;

import com.decisionlense.backend.entity.Sale;
import com.decisionlense.backend.repository.SaleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleRepository saleRepository;

    public SaleController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    // Get all sales
    @GetMapping("/sales")
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    // Total revenue
    @GetMapping("/sales/summary/revenue")
    public Double getTotalRevenue() {
        return saleRepository.getTotalRevenue();
    }

    // Total quantity
    @GetMapping("/sales/summary/quantity")
    public Long getTotalQuantity() {
        return saleRepository.getTotalQuantity();
    }

    // Category-wise revenue
    @GetMapping("/sales/category-revenue")
    public List<Object[]> getCategoryRevenue() {
        return saleRepository.getCategoryRevenue();
    }
}