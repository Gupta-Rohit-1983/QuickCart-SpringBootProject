package com.quickcart.main.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.quickcart.main.model.Cart;
import com.quickcart.main.model.Product;
import com.quickcart.main.model.User;
import com.quickcart.main.repository.CartRepository;
import com.quickcart.main.repository.ProductRepository;
import com.quickcart.main.repository.UserRepository;
import com.quickcart.main.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer pid, Integer uid) {
        User user = userRepository.findById(uid).get();
        Product product = productRepository.findById(pid).get();

        Cart cartStatus = cartRepository.findByProductIdAndUserId(pid, uid);

        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(user);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice());
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
        }
        Cart saveCart = cartRepository.save(cart);

        return saveCart;
    }

    @Override
    public List<Cart> getCartsByUser(Integer uid) {
        List<Cart> carts = cartRepository.findByUserId(uid);

        Double totalOrderPrice = 0.0;

        List<Cart> updatedCarts = new ArrayList<>();
        for (Cart c : carts) {
            Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
            c.setTotalPrice(totalPrice);
            totalOrderPrice += totalPrice;
            c.setTotalOrderAmount(totalOrderPrice);
            updatedCarts.add(c);
        }

        return updatedCarts;
    }

    @Override
    public Integer getCartCount(Integer uid) {
        Integer countByUserId = cartRepository.countByUserId(uid);
        return countByUserId;
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {
        Cart cart = cartRepository.findById(cid).get();
        int updateQuantity;

        if (sy.equalsIgnoreCase("de")) {
            updateQuantity = cart.getQuantity() - 1;
            if (updateQuantity <= 0) {
                cartRepository.deleteById(cid);
            } else {
                cart.setQuantity(updateQuantity);
                cartRepository.save(cart);
            }
        } else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }

    }

}
