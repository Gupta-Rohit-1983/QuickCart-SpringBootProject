package com.quickcart.main.model;

import lombok.Data;

@Data
public class OrderRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String area;

    private String city;

    private String state;

    private String pincode;

    private String paymentType;
}
