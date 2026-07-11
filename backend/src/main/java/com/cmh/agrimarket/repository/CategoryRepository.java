package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
