package com.drivecare.dto;

public class RazorpayOrderResponse {

    private String keyId;
    private String orderId;
    private Long bookingId;
    private Long paymentId;
    private Long amount;
    private String currency;

    public RazorpayOrderResponse() {
    }

    public RazorpayOrderResponse(
            String keyId,
            String orderId,
            Long bookingId,
            Long paymentId,
            Long amount,
            String currency) {

        this.keyId = keyId;
        this.orderId = orderId;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}