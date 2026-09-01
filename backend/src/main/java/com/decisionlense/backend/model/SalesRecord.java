package com.decisionlense.backend.model;

public class SalesRecord {

    private String date;
    private String productId;
    private String category;
    private String region;

    private double quantity;
    private double unitPrice;
    private double cost;
    private double revenue;

    public SalesRecord() {
    }

    public SalesRecord(String date, String productId, String category, String region,
                       double quantity, double unitPrice, double cost, double revenue) {
        this.date = date;
        this.productId = productId;
        this.category = category;
        this.region = region;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.cost = cost;
        this.revenue = revenue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}