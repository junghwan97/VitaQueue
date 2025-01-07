package com.example.orderservice.service;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.OrderProductResponse;

import java.util.List;

public interface OrderService {

    Long saveOrder(List<OrderRequest> orderSaveRequestList, Long userId);
    void cancelOrder(Long orderId, Long userId);
    void returnOrder(Long orderId, Long userId);
    List<OrderProductResponse> getOrder(Long orderId, Long userId);
    List<OrderProductResponse> getOrdersByUserId(Long userId);
    void enterPayment(Long orderId);
}
