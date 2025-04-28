package com.quickcart.main.util;

public enum OrderStatus {

    IN_PROGRESS(1, "in Progress"),
    ORDER_RECEIVED(2, "Received"),
    DISPATCHED(3, "Dispatched"),
    OUT_FOR_DELIVERY(4, "Out for Delivery"),
    DELIVERED(5, "Delivered"),
    CANCEL(6,"Cancelled"),
    SUCCESS(7,"Place Successfully");

    private int id;

    private String status;

    private OrderStatus(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
