
package com.decisionlense.backend.model;

public class InventoryRecord {

    private String date;
    private String productId;
    private String category;

    private double stockAvailable;
    private double stockoutHours;
    private double supplierDelay;

    public InventoryRecord() {
    }

    public InventoryRecord(String date, String productId, String category,
                           double stockAvailable, double stockoutHours,
                           double supplierDelay) {
        this.date = date;
        this.productId = productId;
        this.category = category;
        this.stockAvailable = stockAvailable;
        this.stockoutHours = stockoutHours;
        this.supplierDelay = supplierDelay;
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

    public double getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(double stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public double getStockoutHours() {
        return stockoutHours;
    }

    public void setStockoutHours(double stockoutHours) {
        this.stockoutHours = stockoutHours;
    }

    public double getSupplierDelay() {
        return supplierDelay;
    }

    public void setSupplierDelay(double supplierDelay) {
        this.supplierDelay = supplierDelay;
    }
}