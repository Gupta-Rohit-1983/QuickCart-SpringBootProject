package com.quickcart.main.model;



import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String mobileNumber;

    private String email;

    private String area;

    private String city;

    private String state;

    private String pincode;

    private String password;

    private String role;

    private Boolean isEnable;

    private Boolean accountNonLocked;

    private int failedAttempt;

    private Date lockTime;

    private String resetToken;
}
