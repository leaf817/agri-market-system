-- 按 images 目录为全部演示商品更新封面（可重复执行）
USE agrimarket;

UPDATE product SET cover = '/covers/p1.png'  WHERE id = 1  OR name = '赣南脐橙';
UPDATE product SET cover = '/covers/p2.png'  WHERE id = 2  OR name = '烟台红富士苹果';
UPDATE product SET cover = '/covers/p3.png'  WHERE id = 3  OR name = '赣南蜜桔';
UPDATE product SET cover = '/covers/p4.png'  WHERE id = 4  OR name = '阳光玫瑰葡萄';
UPDATE product SET cover = '/covers/p5.png'  WHERE id = 5  OR name = '高山娃娃菜';
UPDATE product SET cover = '/covers/p6.png'  WHERE id = 6  OR name = '铁棍山药';
UPDATE product SET cover = '/covers/p7.png'  WHERE id = 7  OR name = '有机西红柿';
UPDATE product SET cover = '/covers/p8.png'  WHERE id = 8  OR name = '五常稻花香大米';
UPDATE product SET cover = '/covers/p9.png'  WHERE id = 9  OR name = '农家笨榨花生油';
UPDATE product SET cover = '/covers/p10.png' WHERE id = 10 OR name = '东北玉米糁';
UPDATE product SET cover = '/covers/p11.png' WHERE id = 11 OR name = '手工红薯粉条';
UPDATE product SET cover = '/covers/p12.png' WHERE id = 12 OR name = '野生黑木耳';
UPDATE product SET cover = '/covers/p13.png' WHERE id = 13 OR name = '散养土鸡蛋';
UPDATE product SET cover = '/covers/p14.png' WHERE id = 14 OR name = '安溪铁观音';
UPDATE product SET cover = '/covers/p15.png' WHERE id = 15 OR name = '信阳毛尖';
