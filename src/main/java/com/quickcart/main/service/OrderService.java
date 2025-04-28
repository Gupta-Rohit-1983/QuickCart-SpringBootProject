package com.quickcart.main.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.quickcart.main.model.OrderRequest;
import com.quickcart.main.model.ProductOrder;

public interface OrderService {

    public void saveOrder(Integer userid, OrderRequest orderRequest);

    public List<ProductOrder> getOrdersByUser(Integer userId);

    public ProductOrder updateOrderStatus(Integer id, String status);

    public List<ProductOrder> getAllOrders();

    public ProductOrder getOrderById(String id);

    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);

}
