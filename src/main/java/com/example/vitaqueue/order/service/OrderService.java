package com.example.vitaqueue.order.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.order.domain.entity.OrderEntity;
import com.example.vitaqueue.order.domain.enums.OrderStatus;
import com.example.vitaqueue.order.dto.request.OrderRequest;
import com.example.vitaqueue.order.repository.OrderRepository;
import com.example.vitaqueue.orderProduct.domain.entity.OrderProductEntity;
import com.example.vitaqueue.orderProduct.dto.response.OrderProductResponse;
import com.example.vitaqueue.orderProduct.repository.OrderProductRepository;
import com.example.vitaqueue.product.model.entity.ProductStockEntity;
import com.example.vitaqueue.product.service.ProductService;
import com.example.vitaqueue.security.EncryptionUtil;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public Long saveOrder(List<OrderRequest> orderSaveRequestList, Authentication authentication) throws Exception {
        // 사용자 정보 조회
        UserEntity userEntity = userService.getUserEntity(authentication.getName());
        String address = EncryptionUtil.decrypt(userEntity.getAddress());
        String phone = EncryptionUtil.decrypt(userEntity.getPhone());

        // 주문 엔티티 생성 및 저장
        OrderEntity savedOrder = createOrder(userEntity.getId(), address, phone);

        // 주문 상품 정보 생성 및 저장
        List<OrderProductEntity> orderProductList = createOrderProducts(userEntity.getId(), orderSaveRequestList, savedOrder);
        orderProductRepository.saveAll(orderProductList);

        // 주문의 총 가격 계산 후 저장
        BigDecimal totalPrice = calculateTotalPrice(orderProductList);
        savedOrder.updateTotalPrice(totalPrice);

        // 생성된 주문 ID 반환
        return savedOrder.getId();
    }

    private OrderEntity createOrder(Long userId, String address, String phone) {
        // 용자 ID와 배송 정보를 사용해 OrderEntity를 생성하고 저장
        return orderRepository.save(OrderEntity.builder().userId(userId).deliveryAddress(address).receiverPhone(phone).build());
    }

    private List<OrderProductEntity> createOrderProducts(Long userId, List<OrderRequest> orderSaveRequestList, OrderEntity savedOrder) {
        return orderSaveRequestList.stream()
                .map(orderRequest -> {
                    // 상품 ID로 재고 정보를 조회
                    ProductStockEntity productStock = productService.getProductStockByProductId(orderRequest.getProductId());

                    // 상품 재고를 감소
                    productStock.decreaseStock((long) orderRequest.getQuantity());
                    productService.saveProductStock(productStock);

                    // 주문 상품 엔티티를 생성
                    return OrderProductEntity.builder()
                            .order(savedOrder)
                            .userId(userId)
                            .productId(orderRequest.getProductId())
                            .quantity(orderRequest.getQuantity())
                            .price(productStock.getProduct().getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                            .status(OrderStatus.CREATED)
                            .build();
                }).toList();
    }

    private BigDecimal calculateTotalPrice(List<OrderProductEntity> orderProducts) {
        return orderProducts.stream()
                .map(OrderProductEntity::getPrice) // 각 상품의 가격 조회
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }

    @Transactional
    public void cancelOrder(Long orderId, Authentication authentication) {
        // 사용자 정보 조회
        UserEntity userEntity = userService.getUserEntity(authentication.getName());

        // 주문 ID로 주문 정보를 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));

        // 주문에 대한 권한을 확인
        if (!order.getUserId().equals(userEntity.getId())) {
            throw new SecurityException("해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상태를 확인
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("배송 중 이전에만 주문을 취소할 수 있습니다.");
        }

        // 주문 상태를 취소 완료로 변경
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 해당 주문과 연결된 상품들을 처리
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);
        for (OrderProductEntity orderProduct : orderProducts) {
            // 상품 상태를 취소 완료로 변경
            orderProduct.updateStatus(OrderStatus.CANCELLED);

            // 상품의 재고를 복구
            ProductStockEntity productStock = productService.getProductStockByProductId(orderProduct.getProductId());
            productStock.increaseStock((long) orderProduct.getQuantity());
            productService.saveProductStock(productStock);
        }
    }

    @Transactional
    public void returnOrder(Long orderId, Authentication authentication) {
        // 사용자 인증 정보 조회
        UserEntity userEntity = userService.getUserEntity(authentication.getName());

        // 주문 ID로 주문 정보 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));

        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userEntity.getId())) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_STATE, "배송 완료 상태인 주문만 반품할 수 있습니다.");
        }

        // 반품 가능 기간 확인
        if (!withinReturnPeriod(order.getRegisteredAt())) {
            throw new VitaQueueException(ErrorCode.RETURN_PERIOD_EXPIRED, "반품 가능 기간이 지났습니다.");
        }

        // 주문 상태를 반품 요청으로 변경
        order.updateStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        // 연결된 주문 상품들에 대한 상태와 재고를 처리
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);
        for (OrderProductEntity orderProduct : orderProducts) {
            ProductStockEntity productStock = productService.getProductStockByProductId(orderProduct.getProductId());

            // 상품의 재고를 복구
            productStock.increaseStock((long) orderProduct.getQuantity());
            productService.saveProductStock(productStock);

            // 상품 상태를 반품 완료로 변경
            orderProduct.updateStatus(OrderStatus.RETURNED);
            orderProductRepository.save(orderProduct);
        }
    }

    private boolean withinReturnPeriod(Timestamp registeredAt) {
        return registeredAt != null && registeredAt.toLocalDateTime().plusDays(1).isAfter(LocalDateTime.now());
    }

    public List<OrderProductResponse> getOrder(Long orderId, Authentication authentication) {

        // 사용자 인증 정보 조회
        UserEntity userEntity = userService.getUserEntity(authentication.getName());

        // 주문 ID로 주문 정보 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));

        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userEntity.getId())) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상품 엔티티 목록 조회
        List<OrderProductEntity> orderProductEntities = orderProductRepository.findByOrderId(orderId);

        // 주문 상품 목록을 Response값으로 변환
        List<OrderProductResponse> orderProductResponseList = new ArrayList<>();
        for (OrderProductEntity orderProductEntity : orderProductEntities) {
            ProductStockEntity productStock = productService.getProductStockByProductId(orderProductEntity.getProductId());
            orderProductResponseList.add(OrderProductResponse.fromEntity(orderProductEntity, productStock.getProduct().getName()));
        }
        return orderProductResponseList;
    }
}
