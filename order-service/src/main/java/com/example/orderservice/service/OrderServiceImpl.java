package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
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
    private final StockService stockService;
    private final ProductServiceClient productService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderProductRepository orderProductRepository,
                            StockService stockService,
                            ProductServiceClient productService) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.stockService = stockService;
        this.productService = productService;
    }

    @Override
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
//        stockService.decreaseStock(orderRequest.getProductId(), orderRequest.getQuantity());
        if (stockService.getProduct(orderRequest.getProductId()).getStock() < orderRequest.getQuantity()) {
            throw new VitaQueueException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        BigDecimal price = stockService.getProduct(orderRequest.getProductId()).getPrice();

        return OrderProductEntity.builder()
                .order(savedOrder)
                .userId(userId)
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .price(price.multiply(BigDecimal.valueOf(orderRequest.getQuantity())))
                .status(OrderStatus.CREATED)
                .build();
    }

    private void updateTotalPrice(OrderEntity order, List<OrderProductEntity> orderProducts) {
        BigDecimal totalPrice = orderProducts.stream()
                .map(OrderProductEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.updateTotalPrice(totalPrice);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        // 주문 ID로 주문 정보를 조회
        OrderEntity order = getOrder(orderId);

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
        OrderEntity order = getOrder(orderId);

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
        OrderEntity order = getOrder(orderId);
        // 주문에 대한 권한 확인
        if (!order.getUserId().equals(userId)) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "해당 주문에 대한 권한이 없습니다.");
        }
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


    @Override
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

    @Override
    @Transactional
    public void enterPayment(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new VitaQueueException(ErrorCode.INVALID_ORDER_FOR_PAYMENT, "결제를 위한 주문 상태가 아닙니다.");
        }

        // 주문 상품 리스트 조회
        List<OrderProductEntity> orderProducts = orderProductRepository.findByOrder(order);

        // 모든 상품의 주문 상태 변경
        for (OrderProductEntity orderProduct : orderProducts) {
            orderProduct.updateStatus(OrderStatus.PAYMENT_ENTERED);
        }
        order.updateStatus(OrderStatus.PAYMENT_ENTERED);

        orderRepository.save(order);
    }

    // 주문 조회
    private OrderEntity getOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new VitaQueueException(ErrorCode.ORDER_NOT_FOUND, "해당 주문이 존재하지 않습니다.");
        return order;
    }
}
