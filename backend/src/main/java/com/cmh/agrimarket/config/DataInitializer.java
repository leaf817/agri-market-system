package com.cmh.agrimarket.config;

import com.cmh.agrimarket.entity.Category;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Product;
import com.cmh.agrimarket.repository.CategoryRepository;
import com.cmh.agrimarket.repository.OriginRepository;
import com.cmh.agrimarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 首次启动且分类表为空时，写入演示数据，便于直接体验与答辩展示。
 * 正式使用：删除数据或将本类的条件注释掉即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final CategoryRepository categoryRepo;
    private final OriginRepository originRepo;
    private final ProductRepository productRepo;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepo.count() > 0) {
            return;
        }
        log.info("首次启动，写入演示数据...");

        Category fruit = saveCategory("新鲜水果", "应季水果", 1);
        Category veg = saveCategory("绿色蔬菜", "无公害蔬菜", 2);
        Category grain = saveCategory("粮油米面", "农家自产粮油", 3);
        Category special = saveCategory("乡土特产", "地方特色农产品", 4);

        Origin o1 = saveOrigin("青山生态果园", "浙江省丽水市青田村", "张大为", "13800001111", "绿色食品认证", "高山果园，昼夜温差大，自然成熟不催熟。");
        Origin o2 = saveOrigin("稻香村合作社", "黑龙江省五常市", "李合作社", "13900002222", "有机认证", "黑土地种植，稻花香米核心产区。");
        Origin o3 = saveOrigin("云岭蔬菜基地", "云南省昆明市呈贡区", "王阿姨", "13700003333", "无公害认证", "高原阳光充足，蔬菜清脆甘甜。");

        saveProduct("赣南脐橙", fruit, o1, "8.80", 200, "斤", "皮薄多汁、酸甜可口，现摘现发。", 156);
        saveProduct("烟台红富士苹果", fruit, o1, "6.50", 150, "斤", "脆甜爽口，冰糖心。", 98);
        saveProduct("五常稻花香大米", grain, o2, "12.90", 300, "斤", "黑土地稻花香，软糯香甜。", 230);
        saveProduct("农家笨榨花生油", grain, o2, "35.00", 80, "桶", "传统工艺压榨，香味浓郁。", 64);
        saveProduct("高山娃娃菜", veg, o3, "3.50", 120, "棵", "高原种植，清脆无筋。", 45);
        saveProduct("铁棍山药", veg, o3, "9.90", 90, "斤", "粉糯香甜，营养丰富。", 72);
        saveProduct("手工红薯粉条", special, o1, "15.00", 60, "斤", "纯红薯制作，久煮不烂。", 38);

        log.info("演示数据写入完成。");
    }

    private Category saveCategory(String name, String desc, int sort) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(desc);
        c.setSort(sort);
        return categoryRepo.save(c);
    }

    private Origin saveOrigin(String name, String location, String farmer, String phone, String cert, String desc) {
        Origin o = new Origin();
        o.setName(name);
        o.setLocation(location);
        o.setFarmer(farmer);
        o.setPhone(phone);
        o.setCertificate(cert);
        o.setDescription(desc);
        return originRepo.save(o);
    }

    private Product saveProduct(String name, Category c, Origin o, String price, int stock, String unit, String desc, int sales) {
        Product p = new Product();
        p.setName(name);
        p.setCategory(c);
        p.setOrigin(o);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setUnit(unit);
        p.setDescription(desc);
        p.setSales(sales);
        p.setStatus(1);
        return productRepo.save(p);
    }
}
