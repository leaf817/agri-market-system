package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.dto.ProductRequest;
import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
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

    public List<Product> list(Long categoryId, Integer status, String keyword, Long farmerId) {
        Stream<Product> stream = repo.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream();
        if (farmerId != null) {
            stream = stream.filter(p -> farmerId.equals(p.getFarmerId()));
        }
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

    public List<Product> listForScope(String scope, Long categoryId, Integer status, String keyword, CurrentUser current) {
        String s = scope == null || scope.isBlank() ? "public" : scope;
        if ("public".equalsIgnoreCase(s)) {
            return list(categoryId, 1, keyword, null);
        }
        if ("mine".equalsIgnoreCase(s)) {
            if (current == null || current.role() != Role.FARMER) {
                throw new AuthException(403, "仅农户可查看自己的农产品");
            }
            return list(categoryId, status, keyword, current.id());
        }
        if ("manage".equalsIgnoreCase(s)) {
            if (current == null) {
                throw new AuthException(401, "未登录或登录已过期");
            }
            if (current.role() == Role.ADMIN) {
                return list(categoryId, status, keyword, null);
            }
            if (current.role() == Role.FARMER) {
                return list(categoryId, status, keyword, current.id());
            }
            throw new AuthException(403, "消费者无商品管理权限");
        }
        throw new IllegalArgumentException("不支持的商品查询范围: " + scope);
    }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("商品不存在: " + id));
    }

    @Transactional
    public Product create(ProductRequest req, CurrentUser current) {
        Product p = new Product();
        apply(p, req, current);
        p.setSales(0);
        // 农户上架的产品归属本人；管理员创建的为公共产品（farmerId=null）
        if (current != null && current.role() == Role.FARMER) {
            p.setFarmerId(current.id());
        }
        return repo.save(p);
    }

    @Transactional
    public Product update(Long id, ProductRequest req, CurrentUser current) {
        Product p = get(id);
        checkOwner(p, current);
        apply(p, req, current);
        return repo.save(p);
    }

    @Transactional
    public Product changeStatus(Long id, Integer status, CurrentUser current) {
        Product p = get(id);
        checkOwner(p, current);
        p.setStatus(status);
        return repo.save(p);
    }

    @Transactional
    public void delete(Long id, CurrentUser current) {
        Product p = get(id);
        checkOwner(p, current);
        repo.deleteById(id);
    }

    /** 所有权校验：农户只能操作自己的产品；消费者无写权限。 */
    private void checkOwner(Product p, CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() == Role.FARMER && !current.id().equals(p.getFarmerId())) {
            throw new AuthException(403, "只能管理自己的农产品");
        }
        if (current.role() == Role.CONSUMER) {
            throw new AuthException(403, "消费者无管理权限");
        }
    }

    private void apply(Product p, ProductRequest req, CurrentUser current) {
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
        Origin o = originRepo.findById(req.originId())
                .orElseThrow(() -> new EntityNotFoundException("产地不存在: " + req.originId()));
        if (current != null && current.role() == Role.FARMER && !current.id().equals(o.getFarmerId())) {
            throw new AuthException(403, "农户只能绑定自己的产地");
        }
        p.setOrigin(o);
    }
}
