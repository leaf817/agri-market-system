package com.cmh.agrimarket.service;

import com.cmh.agrimarket.dto.ProductRequest;
import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.CategoryRepository;
import com.cmh.agrimarket.repository.OriginRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;
    private final OriginRepository originRepo;

    public List<Product> list(Long categoryId, Integer status, String keyword) {
        Stream<Product> stream = repo.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream();
        if (categoryId != null) {
            stream = stream.filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()));
        }
        if (status != null) {
            stream = stream.filter(p -> status.equals(p.getStatus()));
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            stream = stream.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw));
        }
        return stream.toList();
    }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("商品不存在: " + id));
    }

    @Transactional
    public Product create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        p.setSales(0);
        return repo.save(p);
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = get(id);
        apply(p, req);
        return repo.save(p);
    }

    @Transactional
    public Product changeStatus(Long id, Integer status) {
        Product p = get(id);
        p.setStatus(status);
        return repo.save(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("商品不存在: " + id);
        }
        repo.deleteById(id);
    }

    private void apply(Product p, ProductRequest req) {
        p.setName(req.name());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setUnit(req.unit());
        p.setCover(req.cover());
        p.setDescription(req.description());
        if (req.status() != null) {
            p.setStatus(req.status());
        }
        if (req.categoryId() != null) {
            Category c = categoryRepo.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("分类不存在: " + req.categoryId()));
            p.setCategory(c);
        } else {
            p.setCategory(null);
        }
        if (req.originId() != null) {
            Origin o = originRepo.findById(req.originId())
                    .orElseThrow(() -> new EntityNotFoundException("产地不存在: " + req.originId()));
            p.setOrigin(o);
        } else {
            p.setOrigin(null);
        }
    }
}
