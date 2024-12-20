package com.example.vitaqueue.product.service;

import com.example.vitaqueue.product.dto.request.ProductRequest;
import com.example.vitaqueue.product.dto.response.ProductResponse;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts();

    ProductResponse getProduct(Long productId);

    void setProduct(ProductRequest request, Collection<? extends GrantedAuthority> authorities);
}
