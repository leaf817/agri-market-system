package com.cmh.agrimarket.common;

import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.entity.Role;
import com.cmh.agrimarket.repository.ProductRepository;
import com.cmh.agrimarket.repository.UserRepository;
import com.cmh.agrimarket.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时确保演示账号存在（幂等）：密码统一为 123456。
 * 并将尚未归属（farmerId 为 null）的演示产品分配给农户账号，
 * 覆盖「脚本初始化」与「ddl-auto 自动建表」两种路径，避免在 SQL 中硬编码密码哈希与外键 id。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        authService.ensureUser("admin", "123456", "系统管理员", Role.ADMIN);
        authService.ensureUser("farmer", "123456", "农户老张", Role.FARMER);
        authService.ensureUser("consumer", "123456", "消费者小李", Role.CONSUMER);

        // 演示产品统一归属 farmer，便于农户登录后直接管理
        userRepository.findByUsername("farmer").ifPresent(farmer -> {
            List<Product> unowned = productRepository.findAll().stream()
                    .filter(p -> p.getFarmerId() == null)
                    .toList();
            if (!unowned.isEmpty()) {
                unowned.forEach(p -> p.setFarmerId(farmer.getId()));
                productRepository.saveAll(unowned);
            }
        });

        log.info("演示账号已就绪：admin / farmer / consumer （默认密码 123456）");
    }
}
