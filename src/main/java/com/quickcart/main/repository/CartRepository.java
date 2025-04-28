package com.quickcart.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quickcart.main.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findByProductIdAndUserId(Integer pid, Integer uid);

    public Integer countByUserId(Integer uid);

    public List<Cart> findByUserId(Integer uid);

}
