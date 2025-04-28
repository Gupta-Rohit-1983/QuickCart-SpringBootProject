package com.quickcart.main.service;

import java.util.List;

import com.quickcart.main.model.Cart;

public interface CartService {

    public Cart saveCart(Integer pid, Integer uid);

    public List<Cart> getCartsByUser(Integer uid);

    public Integer getCartCount(Integer uid);

    public void updateQuantity(String sy, Integer cid);
}
