package com.example.vitaqueue.product.service;

import com.example.vitaqueue.common.exception.ErrorCode;
import com.example.vitaqueue.common.exception.VitaQueueException;
import com.example.vitaqueue.product.dto.request.ProductRequest;
import com.example.vitaqueue.product.dto.request.ProductUpdateRequest;
import com.example.vitaqueue.product.dto.response.ProductResponse;
import com.example.vitaqueue.product.model.entity.ProductEntity;
import com.example.vitaqueue.product.model.entity.ProductStockEntity;
import com.example.vitaqueue.product.repository.ProductRepository;
import com.example.vitaqueue.product.repository.ProductStockRepository;
import com.example.vitaqueue.user.model.entity.UserEntity;
import com.example.vitaqueue.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductStockRepository productStockRepository;

    @SneakyThrows
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

    public ProductResponse getProduct(Long productId) throws Exception {
        // 상품 아이디로 상품 조회 / 존재하지 않으면 예외처리
        ProductEntity productEntity = getProductEntity(productId);
        ProductStockEntity stock = productStockRepository.findByProductId(productId);
        return ProductResponse.fromEntity(productEntity, stock.getStock());
    }


    public void setProduct(ProductRequest request, Authentication authentication) {
        // 유저 권한 확인
        extracted(authentication);
        // 관리자 권한이면 상품 저장
        UserEntity userEntity = getUserEntity(authentication.getName());
        ProductEntity productEntity = productRepository.save(new ProductEntity().of(request, userEntity));
        // 재고 정보 저장
        productStockRepository.save(new ProductStockEntity().of(productEntity, request.getStock()));
    }


    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request, Authentication authentication) {
        // 권한 확인
        extracted(authentication);
        ProductEntity productEntity = getProductEntity(productId);
        if (request.getName() != null) productEntity.setName(request.getName());
        if (request.getPrice() != null) productEntity.setPrice(request.getPrice());
        if (request.getDescript() != null) productEntity.setDescript(request.getDescript());
    }

    @Transactional
    public void deleteProduct(Long productId, Authentication authentication) {
        extracted(authentication);
        ProductEntity productEntity = getProductEntity(productId);
        productRepository.delete(productEntity);
    }

    // 관리자 권한 확인
    private static void extracted(Authentication authentication) {
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!hasAdminRole) {
            throw new VitaQueueException(ErrorCode.INVALID_PERMISSION, "상품에 대한 권한이 없습니다.");
        }
    }

    // 유저 확인
    private UserEntity getUserEntity(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new VitaQueueException(ErrorCode.USER_NOT_FOUND, "등록되지 않은 메일입니다."));
    }

    //상품 확인
    private ProductEntity getProductEntity(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new VitaQueueException(ErrorCode.PRODUCT_NOT_FOUND, String.format("%d번 상품은 존재하지 않습니다.", productId)));
    }

}
