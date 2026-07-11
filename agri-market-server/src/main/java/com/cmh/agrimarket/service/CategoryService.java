package com.cmh.agrimarket.service;

import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;

    public List<Category> list() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "sort", "id"));
    }

    @Transactional
    public Category save(Category category) {
        return repo.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("分类不存在: " + id);
        }
        repo.deleteById(id);
    }
}
