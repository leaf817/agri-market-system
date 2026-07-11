package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
