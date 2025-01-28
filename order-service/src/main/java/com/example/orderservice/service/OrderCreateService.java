package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service

public class OrderCreateService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productService;
    private final RedissonClient redissonClient;

    public OrderCreateService(OrderRepository orderRepository,
                              OrderProductRepository orderProductRepository,
                              ProductServiceClient productService,
                              RedissonClient redissonClient) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
        this.redissonClient = redissonClient;
    }

    // 주문 생성
    @Transactional
    public Long saveOrder(List<OrderRequest> orderSaveRequestList, Long userId) {

        validateOrderRequest(orderSaveRequestList);

        String address = extractAddress(orderSaveRequestList);
        String phone = extractPhone(orderSaveRequestList);

        OrderEntity savedOrder = createOrder(userId, address, phone);

        List<OrderProductEntity> orderProductList = createOrderProducts(userId, orderSaveRequestList, savedOrder);
        orderProductRepository.saveAll(orderProductList);

        updateTotalPrice(savedOrder, orderProductList);

        return savedOrder.getId();
    }

    private void validateOrderRequest(List<OrderRequest> orderRequests) {
        if (orderRequests == null || orderRequests.isEmpty()) {
            throw new IllegalArgumentException("주문 요청이 비어 있습니다.");
        }
    }

    private String extractAddress(List<OrderRequest> orderRequests) {
        return orderRequests.get(0).getAddress();
    }

    private String extractPhone(List<OrderRequest> orderRequests) {
        return orderRequests.get(0).getPhone();
    }

    private OrderEntity createOrder(Long userId, String address, String phone) {
        return orderRepository.save(OrderEntity.builder()
                .userId(userId)
                .deliveryAddress(address)
                .receiverPhone(phone)
                .build());
    }

    private List<OrderProductEntity> createOrderProducts(Long userId, List<OrderRequest> orderRequests, OrderEntity savedOrder) {
        return orderRequests.stream()
                .map(orderRequest -> createOrderProduct(userId, savedOrder, orderRequest))
                .toList();
    }

    private OrderProductEntity createOrderProduct(Long userId, OrderEntity savedOrder, OrderRequest orderRequest) {
        // Step 1: 재고 예약 요청
        boolean reserved = productService.reserveStock(orderRequest.getProductId(), orderRequest.getQuantity()).getResult();
        if (!reserved) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH, "재고가 부족합니다.");
        }

        // Step 2: 상품 가격 조회
        BigDecimal price = productService.getProduct(orderRequest.getProductId()).getResult().getPrice();

        return OrderProductEntity.builder()
                .order(savedOrder)
                .userId(userId)
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .price(price.multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                .status(OrderStatus.RESERVED)
                .build();
    }

    private void updateTotalPrice(OrderEntity order, List<OrderProductEntity> orderProducts) {
        BigDecimal totalPrice = orderProducts.stream()
                .map(OrderProductEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.updateTotalPrice(totalPrice);
    }

}
