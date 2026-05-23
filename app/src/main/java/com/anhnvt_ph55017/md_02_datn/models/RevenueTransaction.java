package com.anhnvt_ph55017.md_02_datn.models;

public class RevenueTransaction {
    private final String orderNumber;
    private final String productName;
    private final String productImage;
    private final String sku;
    private final int quantity;
    private final double grossAmount;
    private final double platformFee;
    private final double netAmount;
    private final String paidAt;
    private final String status;

    public RevenueTransaction(String orderNumber, String productName, String productImage,
                              String sku, int quantity, double grossAmount,
                              double platformFee, double netAmount, String paidAt,
                              String status) {
        this.orderNumber = orderNumber;
        this.productName = productName;
        this.productImage = productImage;
        this.sku = sku;
        this.quantity = quantity;
        this.grossAmount = grossAmount;
        this.platformFee = platformFee;
        this.netAmount = netAmount;
        this.paidAt = paidAt;
        this.status = status;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getGrossAmount() {
        return grossAmount;
    }

    public double getPlatformFee() {
        return platformFee;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getStatus() {
        return status;
    }
}
