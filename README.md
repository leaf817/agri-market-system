# 农产品信息展示与售卖管理系统（乡村助农实战项目）

> 课程实训 · 乡村助农类特色实战项目 · Spring Boot 3 + Vue 3 全栈

面向乡村助农场景的农产品信息展示与售卖管理系统：农户/管理员可上架农产品、公示产地溯源，系统支持分类展示、下单与订单流转、销量与销售额统计。

## ✨ 已实现功能

- ✅ **农产品上架/管理**：新增、编辑、上下架、删除，含价格、库存、单位、封面、描述
- ✅ **分类展示**：分类增删改，产品按分类筛选
- ✅ **产地信息公示**：产地/基地、农户、联系方式、资质认证、产地介绍的维护与展示
- ✅ **订单管理**：模拟下单（自动扣减库存、累加销量）、订单明细、状态流转（待付款→待发货→已发货→已完成/已取消，取消自动回滚库存与销量）
- ✅ **销量统计**：经营总览（商品数/订单数/累计销量/销售额）、各品类销量与销售额、销量 TOP 商品

## 🛠 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.3.5、Java 17、Spring Data JPA、MySQL 8、Maven、Lombok |
| 前端 | Vue 3、Vite 5、Element Plus、Vue Router、Axios |

## 📁 目录结构

```
agri-market-system/
├── backend/                         # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cmh/agrimarket/
│       │   ├── AgriMarketApplication.java
│       │   ├── common/        # 统一返回、全局异常、跨域
│       │   ├── config/        # 启动初始化演示数据
│       │   ├── entity/        # JPA 实体（产品/分类/产地/订单/订单项）
│       │   ├── dto/           # 请求与统计返回对象
│       │   ├── repository/    # Spring Data JPA
│       │   ├── service/       # 业务逻辑
│       │   └── controller/    # REST 接口（/api/**）
│       └── resources/application.yml
├── frontend/                        # Vue 3 前端
│   ├── package.json、vite.config.js、index.html
│   └── src/
│       ├── main.js、App.vue
│       ├── api/               # axios 封装与接口
│       ├── router/            # 路由
│       └── views/             # 产品/分类/产地/订单/统计 五个页面
├── start-backend.bat                # Windows 一键启动后端
├── start-frontend.bat               # Windows 一键启动前端
└── README.md
```

## ✅ 环境要求

- JDK 17+（本机 Maven 已使用 JDK 17）
- Maven 3.6+
- MySQL 8.0（服务需处于运行状态）
- Node.js 18+ 与 npm

## 🚀 快速开始

**1. 配置数据库密码**

打开 `backend/src/main/resources/application.yml`，把 `spring.datasource.password` 改成你本机 MySQL 的 root 密码。

> 连接 URL 中带 `createDatabaseIfNotExist=true`，启动时若 `agrimarket` 库不存在会自动创建，**无需手动建库**；表由 JPA 自动创建，首次启动还会写入演示数据。

**2. 启动后端**（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

> 或在 IDEA 中导入 `backend` 目录为 Maven 项目，运行 `AgriMarketApplication`。

**3. 启动前端**（端口 5173）

```bash
cd frontend
npm install      # 首次执行
npm run dev
```

**4. 访问**：浏览器打开 <http://localhost:5173>

> Windows 也可双击根目录的 `start-backend.bat` 与 `start-frontend.bat` 启动。

## 🔌 主要接口（前缀 `/api`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST/DELETE | `/categories` | 分类列表 / 新增·修改 / 删除 |
| GET/POST/DELETE | `/origins` | 产地列表 / 新增·修改 / 删除 |
| GET/POST | `/products` | 产品列表（支持 `categoryId/status/keyword`）/ 新增 |
| GET/PUT/DELETE | `/products/{id}` | 详情 / 修改 / 删除 |
| PATCH | `/products/{id}/status?status=1\|0` | 上下架 |
| GET/POST | `/orders` | 订单列表 / 创建订单 |
| GET | `/orders/{id}` | 订单明细 |
| PATCH | `/orders/{id}/status?status=PAID` | 变更订单状态 |
| GET | `/stats/overview`、`/stats/by-category`、`/stats/top-products` | 销量统计 |

统一返回结构：`{ "code": 0, "message": "success", "data": ... }`，`code=0` 为成功。

## ❓ 常见问题

- **Lombok 报错**：IDEA 需安装 Lombok 插件并开启 *Settings → Build → Compiler → Annotation Processors → Enable*。
- **连不上 MySQL**：确认 MySQL 服务已启动，并核对 `application.yml` 中的用户名/密码。
- **端口被占用**：后端默认 8080、前端默认 5173，可在 `application.yml` / `vite.config.js` 修改。
- **清空演示数据**：在 MySQL 中执行 `DROP DATABASE agrimarket;` 后重启后端即可重新初始化。

## 🌱 后续可扩展

用户登录与权限（管理员/农户/消费者）、图片上传、支付对接、物流、评价与收藏、数据大屏（ECharts）等。

## 作者

- 陈明欢（课程实训）
