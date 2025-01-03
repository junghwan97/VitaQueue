package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.request.StockRequest;
import com.example.orderservice.dto.response.OrderProductResponse;
import com.example.orderservice.dto.response.ProductResponse;
import com.example.orderservice.dto.response.ProductStockResponse;
import com.example.orderservice.exception.ErrorCode;
import com.example.orderservice.exception.VitaQueueException;
import com.example.orderservice.jpa.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final UserServiceClient userService;
    private final ProductServiceClient productService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderProductRepository orderProductRepository,
                            UserServiceClient userService,
                            ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Long saveOrder(List<OrderRequest> orderSaveRequestList, Long userId) {
        // 사용자 정보 조회
        String address = orderSaveRequestList.get(0).getAddress();
        String phone = orderSaveRequestList.get(0).getPhone();

        // 주문 엔티티 생성 및 저장
        OrderEntity savedOrder = createOrder(userId, address, phone);

        // 주문 상품 정보 생성 및 저장
        List<OrderProductEntity> orderProductList = createOrderProducts(userId, orderSaveRequestList, savedOrder);
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
                    ProductStockResponse productStock = productService.checkCount(orderRequest.getProductId());
                    // 상품 재고를 감소
                    productStock.decreaseStock((long) orderRequest.getQuantity());
                    StockRequest stock = new StockRequest(productStock.getProductId(), productStock.getStock());
                    productService.saveProductStock(stock);
                    BigDecimal price = productService.getProduct(productStock.getProductId()).getPrice();
                    // 주문 상품 엔티티를 생성
                    return OrderProductEntity.builder()
                            .order(savedOrder)
                            .userId(userId)
                            .productId(orderRequest.getProductId())
                            .quantity(orderRequest.getQuantity())
                            .price(price.multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                            .status(OrderStatus.CREATED)
                            .build();
                }).toList();
    }

    private BigDecimal calculateTotalPrice(List<OrderProductEntity> orderProducts) {
        return orderProducts.stream()
                .map(OrderProductEntity::getPrice) // 각 상품의 가격 조회
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }


    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보를 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));

        // 주문에 대한 권한을 확인
        if (!order.getUserId().equals(userId)) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }

        // 주문 상태를 확인
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new VitaQueueException(ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED, "배송 중 이전에만 주문을 취소할 수 있습니다.");
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
            ProductStockResponse productStock = productService.checkCount(orderProduct.getProductId());
            productStock.increaseStock((long) orderProduct.getQuantity());
            StockRequest stock = new StockRequest(productStock.getProductId(), productStock.getStock());
            productService.saveProductStock(stock);
        }
    }

    @Override
    @Transactional
    public void returnOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));

        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userId)) {
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
//            ProductStockEntity productStock = productService.getProductStockByProductId(orderProduct.getProductId());
            ProductStockResponse productStock = productService.checkCount(orderProduct.getProductId());

            // 상품의 재고를 복구
            productStock.increaseStock((long) orderProduct.getQuantity());
            StockRequest stock = new StockRequest(productStock.getProductId(), productStock.getStock());
            productService.saveProductStock(stock);

            // 상품 상태를 반품 완료로 변경
            orderProduct.updateStatus(OrderStatus.RETURNED);
            orderProductRepository.save(orderProduct);
        }
    }

    private boolean withinReturnPeriod(Timestamp registeredAt) {
        return registeredAt != null && registeredAt.toLocalDateTime().plusDays(1).isAfter(LocalDateTime.now());
    }

    @Override
    public List<OrderProductResponse> getOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다."));
        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userId)) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }
        // 주문 상품 엔티티 목록 조회
        List<OrderProductEntity> orderProductEntities = orderProductRepository.findByOrderId(orderId);

        // 주문 상품 목록을 Response값으로 변환
        List<OrderProductResponse> orderProductResponseList = new ArrayList<>();
        for (OrderProductEntity orderProductEntity : orderProductEntities) {
            ProductResponse productResponse = productService.getProduct(orderProductEntity.getProductId());
            orderProductResponseList.add(OrderProductResponse.fromEntity(orderProductEntity, productResponse.getName()));
        }
        return orderProductResponseList;
    }

    @Override
    public List<OrderProductResponse> getOrdersByUserId(Long userId) {
        // 주문 정보 조회
        List<OrderEntity> orders = orderRepository.findByUserId(userId);

        if (orders == null || orders.isEmpty()) {
//            throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND);
            return null;
        }

        // 상세 주문 정보 리스트 초기화
        List<OrderProductResponse> orderProductResponseList = new ArrayList<>();

        // 각 주문에 대한 상세 주문 조회 및 추가
        for (OrderEntity orderEntity : orders) {
            List<OrderProductEntity> products = orderProductRepository.findByOrderId(orderEntity.getId());

            for (OrderProductEntity product : products) {
                ProductResponse productResponse = productService.getProduct(product.getProductId());
                // `OrderProductResponse` 객체로 변환
                OrderProductResponse response = OrderProductResponse.fromEntity(
                        product,
                        productResponse.getName()
                         // 필요한 추가 데이터 매핑
                );
                orderProductResponseList.add(response); // 변환된 객체를 리스트에 추가
            }
        }

        return orderProductResponseList;
    }
}
