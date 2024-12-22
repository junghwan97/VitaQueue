package com.example.vitaqueue.product.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.product.dto.request.ProductRequest;
import com.example.vitaqueue.product.dto.response.ProductResponse;
import com.example.vitaqueue.product.model.entity.ProductEntity;
import com.example.vitaqueue.product.model.entity.ProductStockEntity;
import com.example.vitaqueue.product.repository.ProductRepository;
import com.example.vitaqueue.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService{

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    public List<ProductResponse> getProducts(Long cursorId, Integer size) {
        List<ProductEntity> productEntityList;
        // 커서가 0이라면 최신 등록 제품를 size만큼 반환
        if (cursorId == 0) productEntityList = productRepository.findAllOrderByIdDesc(size);
        // 커서가 0이 아니라면 cursor번 등록 제품부터 size만큼 반환
        else productEntityList = productRepository.findAllOrderByIdDescWithCursor(cursorId, size);

        return productEntityList.stream().map(ProductResponse::fromEntity).toList();
    }

    public ProductResponse getProduct(Long productId) {
        // 상품 아이디로 상품 조회 / 존재하지 않으면 예외처리
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.PRODUCT_NOT_FOUND, String.format("%d번 상품은 존재하지 않습니다.", productId)));
        return ProductResponse.fromEntity(productEntity);
    }

    public void setProduct(ProductRequest request, Collection<? extends GrantedAuthority> authorities) {
        // 유저 권한 확인
        if (!authorities.equals("ROLE_ADMIN")) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "상품 등록 권한이 없습니다.");
        }
        // 관리자 권한이면 상품 저장
        ProductEntity productEntity = productRepository.save(new ProductEntity().of(request));
        // 재고 정보 저장
        productStockRepository.save(new ProductStockEntity().of(productEntity, request.getStock()));
    }

}
