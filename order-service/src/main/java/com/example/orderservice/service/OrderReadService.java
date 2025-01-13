package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.response.OrderProductResponse;
import com.example.orderservice.dto.response.ProductResponse;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.jpa.OrderProductEntity;
import com.example.orderservice.jpa.OrderProductRepository;
import com.example.orderservice.jpa.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderReadService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productService;

    public OrderReadService(OrderRepository orderRepository,
                            OrderProductRepository orderProductRepository,
                            ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
    }

    // 특정 주문 상세 조회
    public List<OrderProductResponse> getOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보 조회
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(null);
        if (order == null) throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다.");

        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userId)) throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");

        // 주문 상품 엔티티 목록 조회
        List<OrderProductEntity> orderProductEntities = orderProductRepository.findByOrderId(orderId);

        // 주문 상품 목록을 Response값으로 변환
        List<OrderProductResponse> orderProductResponseList = new ArrayList<>();
        for (OrderProductEntity orderProductEntity : orderProductEntities) {
            ProductResponse productResponse = productService.getProduct(orderProductEntity.getProductId()).getResult();
            orderProductResponseList.add(OrderProductResponse.fromEntity(orderProductEntity, productResponse.getName()));
        }
        return orderProductResponseList;
    }

    // 특정 사용자의 모든 주문 목록 조회
    public List<OrderProductResponse> getOrdersByUserId(Long userId) {
        // 주문 정보 조회
        List<OrderEntity> orders = orderRepository.findByUserId(userId);
        if (orders == null) return null;

        // 상세 주문 정보 리스트 초기화
        List<OrderProductResponse> orderProductResponseList = new ArrayList<>();

        // 각 주문에 대한 상세 주문 조회 및 추가
        for (OrderEntity orderEntity : orders) {
            List<OrderProductEntity> products = orderProductRepository.findByOrderId(orderEntity.getId());

            for (OrderProductEntity product : products) {
                ProductResponse productResponse = productService.getProduct(product.getProductId()).getResult();
                OrderProductResponse response = OrderProductResponse.fromEntity(
                        product,
                        productResponse.getName()
                );
                orderProductResponseList.add(response); // 변환된 객체를 리스트에 추가
            }
        }
        return orderProductResponseList;
    }
}
