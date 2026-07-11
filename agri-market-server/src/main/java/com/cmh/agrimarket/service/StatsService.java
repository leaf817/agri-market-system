package com.cmh.agrimarket.service;

import com.cmh.agrimarket.dto.CategorySales;
import com.cmh.agrimarket.dto.StatsOverview;
import com.cmh.agrimarket.dto.TopProduct;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.OrderItemRepository;
import com.cmh.agrimarket.repository.OrderRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;

    /** farmerId 为 null 表示管理员（全局），否则仅统计该农户的产品范围。 */
    public StatsOverview overview(Long farmerId) {
        List<Product> products = productsOf(farmerId);
        long totalQuantity = products.stream().mapToLong(p -> p.getSales() == null ? 0 : p.getSales()).sum();
        BigDecimal totalAmount = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getSales() == null ? 0 : p.getSales())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long orderCount = countOrders(farmerId);
        return new StatsOverview(products.size(), orderCount, totalQuantity, totalAmount);
    }

    public List<CategorySales> salesByCategory(Long farmerId) {
        Map<String, long[]> qtyMap = new HashMap<>();
        Map<String, BigDecimal> amountMap = new HashMap<>();
        Map<String, Long> idMap = new HashMap<>();
        for (Product p : productsOf(farmerId)) {
            String name = p.getCategory() == null ? "未分类" : p.getCategory().getName();
            Long cid = p.getCategory() == null ? null : p.getCategory().getId();
            int sales = p.getSales() == null ? 0 : p.getSales();
            qtyMap.computeIfAbsent(name, k -> new long[]{0})[0] += sales;
            amountMap.merge(name, p.getPrice().multiply(BigDecimal.valueOf(sales)), BigDecimal::add);
            idMap.put(name, cid);
        }
        return qtyMap.keySet().stream()
                .map(name -> new CategorySales(idMap.get(name), name, qtyMap.get(name)[0],
                        amountMap.getOrDefault(name, BigDecimal.ZERO)))
                .sorted(Comparator.comparingLong(CategorySales::quantity).reversed())
                .collect(Collectors.toList());
    }

    public List<TopProduct> topProducts(int limit, Long farmerId) {
        return productsOf(farmerId).stream()
                .sorted(Comparator.comparingInt((Product p) -> p.getSales() == null ? 0 : p.getSales()).reversed())
                .limit(limit)
                .map(p -> new TopProduct(p.getId(), p.getName(), p.getSales(),
                        p.getPrice().multiply(BigDecimal.valueOf(p.getSales() == null ? 0 : p.getSales()))))
                .collect(Collectors.toList());
    }

    private List<Product> productsOf(Long farmerId) {
        return farmerId == null ? productRepo.findAll() : productRepo.findByFarmerId(farmerId);
    }

    private long countOrders(Long farmerId) {
        if (farmerId == null) {
            return orderRepo.count();
        }
        List<Long> myProductIds = productRepo.findByFarmerId(farmerId).stream()
                .map(Product::getId).toList();
        if (myProductIds.isEmpty()) {
            return 0;
        }
        return orderItemRepo.findDistinctOrderIdByProductIdIn(myProductIds).size();
    }
}
