package com.quickcart.main.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quickcart.main.model.Cart;
import com.quickcart.main.model.OrderAddress;
import com.quickcart.main.model.OrderRequest;
import com.quickcart.main.model.ProductOrder;
import com.quickcart.main.repository.CartRepository;
import com.quickcart.main.repository.ProductOrderRepository;
import com.quickcart.main.service.OrderService;
import com.quickcart.main.util.CommonUtil;
import com.quickcart.main.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userid, OrderRequest orderRequest) {
        List<Cart> carts = cartRepository.findByUserId(userid);

        for (Cart cart : carts) {
            ProductOrder order = new ProductOrder();

            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setOrderQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.SUCCESS.getStatus());
            order.setPaymentType(orderRequest.getPaymentType());

            OrderAddress address = new OrderAddress();

            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNumber(orderRequest.getMobileNumber());
            address.setArea(orderRequest.getArea());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());

            order.setOrderAddress(address);

            ProductOrder saveOrder = productOrderRepository.save(order);

            try {
                commonUtil.productOrderEmail(saveOrder, OrderStatus.SUCCESS.getStatus());
            } catch (Exception e) {
            }
        }
    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = productOrderRepository.findByUserId(userId);
        return orders;
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> byId = productOrderRepository.findById(id);

        if (byId.isPresent()) {
            ProductOrder productOrder = byId.get();
            productOrder.setStatus(status);
            ProductOrder updateOrder = productOrderRepository.save(productOrder);
            return updateOrder;
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return productOrderRepository.findAll();
    }

    

    @Override
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productOrderRepository.findAll(pageable);
    }

    @Override
    public ProductOrder getOrderById(String id) {
      return productOrderRepository.findByOrderId(id);
    }   
}
