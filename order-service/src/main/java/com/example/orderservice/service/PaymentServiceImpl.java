package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService{

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
//    private final StockService stockService;
    private final ProductServiceClient productService;

    public PaymentServiceImpl(OrderRepository orderRepository,
                              OrderProductRepository orderProductRepository,
//                              StockService stockService,
                              ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
//        this.stockService = stockService;
        this.productService = productService;
    }

    @Transactional
    public String processPayment(Long orderId) {
        // 주문 조회
        OrderEntity order = getOrder(orderId);

        // 상태 확인: 결제 가능한 상태인지 확인
        if (order.getStatus() != OrderStatus.PAYMENT_ENTERED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_FOR_PAYMENT, "결제를 위한 주문 상태가 아닙니다.");
        }

        // 첫 번째 20% 실패 시뮬레이션: 결제 시도 실패
        if (Math.random() < 0.2) {
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
            updateAllOrderProductStatus(order, OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            return "결제 시도 단계에서 결제 실패"; // 첫 번째 실패
        }

        // 두 번째 20% 실패 시뮬레이션: 결제 중 실패
        if (Math.random() >= 0.2) {
            // 결제 성공 처리
            order.updateStatus(OrderStatus.PAYMENT_SUCCESS);

            // 주문 상품 리스트 조회
            List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);

            // 모든 상품의 재고 차감
            for (OrderProductEntity orderProduct : orderProducts) {
//                stockService.decreaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
                productService.decreaseStock(orderProduct.getProductId(), orderProduct.getQuantity());
                orderProduct.updateStatus(OrderStatus.PAYMENT_SUCCESS);
            }
        } else {
            // 두 번째 실패
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
            updateAllOrderProductStatus(order, OrderStatus.PAYMENT_FAILED);
            return "결제중 단계에서 결제 실패";
        }

        // 주문 상태 저장
        orderRepository.save(order);
        return "결제가 완료되었습니다.";
    }

    // 주문 조회
    private OrderEntity getOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다.");
        return order;
    }

    private void updateAllOrderProductStatus(OrderEntity order, OrderStatus status) {
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);

        for (OrderProductEntity orderProduct : orderProducts) {
            orderProduct.updateStatus(status);
        }

        orderProductRepository.saveAll(orderProducts);
    }
}
