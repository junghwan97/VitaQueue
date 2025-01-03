package com.example.productservice.service;


import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.StockRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.exception.ErrorCode;
import com.example.productservice.exception.VitaQueueException;
import com.example.productservice.jpa.ProductEntity;
import com.example.productservice.jpa.ProductRepository;
import com.example.productservice.jpa.ProductStockEntity;
import com.example.productservice.jpa.ProductStockRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductStockRepository productStockRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }

    @Override
    public List<ProductResponse> getProducts(Long cursorId, Integer size) {
        List<ProductEntity> productEntityList;
        // 커서가 0이라면 최신 등록 제품를 size만큼 반환
        if (cursorId == 0) productEntityList = productRepository.findAllOrderByIdDesc(size);
            // 커서가 0이 아니라면 cursor번 등록 제품부터 size만큼 반환
        else productEntityList = productRepository.findAllOrderByIdDescWithCursor(cursorId, size);

        List<ProductResponse> productResponses = new ArrayList<>();
        for (ProductEntity productEntity : productEntityList) {
            productResponses.add(ProductResponse.fromEntity(productEntity));
        }
        return productResponses;
    }

    @Override
    public ProductResponse getProduct(Long productId) throws Exception {
        // 상품 아이디로 상품 조회 / 존재하지 않으면 예외처리
        ProductEntity productEntity = getProductEntity(productId);
        ProductStockEntity stock = getProductStockByProductId(productId);
        return ProductResponse.fromEntity(productEntity, stock.getStock());
    }

    @Override
    public void setProduct(ProductRequest request, Long userId, String role) {
        // 유저 권한 확인
        extracted(role);
        // 관리자 권한이면 상품 저장
        ProductEntity productEntity = productRepository.save(new ProductEntity().of(request, userId));
        // 재고 정보 저장
        productStockRepository.save(new ProductStockEntity().of(productEntity.getId(), request.getStock()));
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request, Long userId, String role) {
        // 권한 확인
        extracted(role);
        ProductEntity productEntity = getProductEntity(productId);
        if (!productEntity.getUserId().equals(userId)) throw new VitaQueueException(ErrorCode.INVALID_PERMISSION);
        if (request.getName() != null) productEntity.setName(request.getName());
        if (request.getPrice() != null) productEntity.setPrice(request.getPrice());
        if (request.getDescript() != null) productEntity.setDescript(request.getDescript());
    }

    @Override
    public void deleteProduct(Long productId, String role) {
        extracted(role);
        ProductEntity productEntity = getProductEntity(productId);
        productRepository.delete(productEntity);
    }

    // 관리자 권한 확인
    private static void extracted(String role) {
        if (!role.equals("ROLE_ADMIN")) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "상품에 대한 권한이 없습니다.");
        }
    }

    //상품 확인
    public ProductEntity getProductEntity(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.PRODUCT_NOT_FOUND, String.format("%d번 상품은 존재하지 않습니다.", productId)));
    }

    // 상품 재고 등록 확인
    public ProductStockEntity getProductStockByProductId(Long productId) {
        return productStockRepository.findByProductId(productId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND, "상품 재고 정보를 찾을 수 없습니다."));
    }

    // 재고 저장
    @Transactional
    public void saveProductStock(StockRequest stockRequest) {
        ProductStockEntity productStockEntity = productStockRepository.findByProductId(stockRequest.getProductId()).orElseThrow(
                ()-> new VitaQueueException(ErrorCode.PRODUCT_STOCK_NOT_FOUND)
        );
        productStockEntity.setProductId(stockRequest.getProductId());
        productStockEntity.setStock(stockRequest.getStock());
    }
}
