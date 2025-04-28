package com.quickcart.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quickcart.main.model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

    List<ProductOrder> findByUserId(Integer userId);

    ProductOrder findByOrderId(String orderId);

}
