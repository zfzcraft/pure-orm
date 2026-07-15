package cn.zfz.pureorm.test.h2;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.MapperFactory;
import cn.zfz.pureorm.core.Page;
import cn.zfz.pureorm.crud.select.single.LambadaSelectWrapper;
import cn.zfz.pureorm.crud.update.LambadaUpdateWrapper;

/**
 * 测试 MapperFactory 动态代理创建自定义 Mapper 接口
 * 验证：建表 -> MapperFactory.create() -> CRUD + Upsert + 分页
 */
public class PureOrmMapperFactoryTest {

    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;MODE=MySQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        DataSource dataSource = new HikariDataSource(config);

        // 1. 建表
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE t_product (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100), " +
                    "price INT, " +
                    "category VARCHAR(50)" +
                    ")");
            System.out.println("✓ 表 t_product 创建成功");
        }

        // 2. 通过 MapperFactory 创建动态代理 Mapper
        TestProductMapper productMapper = MapperFactory.create(TestProductMapper.class, dataSource);
        System.out.println("✓ MapperFactory.create(TestProductMapper.class) 代理创建成功");
        System.out.println("  代理类: " + productMapper.getClass().getName());

        // 3. 测试插入
        System.out.println("\n========== 1. 插入 ==========");
        TestProduct p1 = new TestProduct();
        p1.setName("iPhone 15");
        p1.setPrice(5999);
        p1.setCategory("手机");
        productMapper.insert(p1);
        System.out.println("插入成功, 自增ID: " + p1.getId());

        TestProduct p2 = new TestProduct();
        p2.setName("MacBook Pro");
        p2.setPrice(12999);
        p2.setCategory("电脑");
        productMapper.insert(p2);

        TestProduct p3 = new TestProduct();
        p3.setName("iPad Air");
        p3.setPrice(4399);
        p3.setCategory("平板");
        productMapper.insert(p3);

        TestProduct p4 = new TestProduct();
        p4.setName("AirPods Pro");
        p4.setPrice(1999);
        p4.setCategory("配件");
        productMapper.insert(p4);

        System.out.println("共插入 4 条商品");

        // 4. 按ID查询
        System.out.println("\n========== 2. 按ID查询 ==========");
        TestProduct found = productMapper.selectByPrimaryKey(p1.getId());
        System.out.println("查询结果: id=" + found.getId() + ", name=" + found.getName() + ", price=" + found.getPrice());
        if (!"iPhone 15".equals(found.getName())) {
            throw new AssertionError("查询结果不对");
        }

        // 5. Lambda 条件查询
        System.out.println("\n========== 3. Lambda 条件查询（price > 4000） ==========");
        LambadaSelectWrapper<TestProduct> wrapper = LambadaSelectWrapper.of();
        wrapper.gt(TestProduct::getPrice, 4000);
        wrapper.orderByDesc(TestProduct::getPrice);
        List<TestProduct> list = productMapper.selectList(wrapper);
        for (TestProduct p : list) {
            System.out.println("  " + p.getName() + " - ¥" + p.getPrice());
        }
        if (list.size() != 3) {
            throw new AssertionError("应该有3条价格>4000的商品, 实际: " + list.size());
        }

        // 6. 更新
        System.out.println("\n========== 4. 更新 ==========");
        p1.setPrice(5499);
        productMapper.updateByPrimaryKey(p1);
        TestProduct updated = productMapper.selectByPrimaryKey(p1.getId());
        System.out.println("更新后价格: " + updated.getPrice());
        if (updated.getPrice() != 5499) {
            throw new AssertionError("更新失败");
        }

        // 7. LambdaUpdateWrapper
        System.out.println("\n========== 5. LambdaUpdateWrapper ==========");
        LambadaUpdateWrapper<TestProduct> updateWrapper = LambadaUpdateWrapper.of();
        updateWrapper.set(TestProduct::getCategory, "智能手机");
        updateWrapper.eq(TestProduct::getId, p1.getId());
        productMapper.update(updateWrapper);
        TestProduct lambdaUpdated = productMapper.selectByPrimaryKey(p1.getId());
        System.out.println("Lambda更新后 category: " + lambdaUpdated.getCategory());
        if (!"智能手机".equals(lambdaUpdated.getCategory())) {
            throw new AssertionError("Lambda更新失败");
        }

        // 8. 分页查询
        System.out.println("\n========== 6. 分页查询 ==========");
        LambadaSelectWrapper<TestProduct> pageWrapper = LambadaSelectWrapper.of();
        pageWrapper.orderByAsc(TestProduct::getId);
        Page<TestProduct> page = productMapper.selectPage(pageWrapper, 1, 2);
        System.out.println("第1页, 每页2条, 总记录数: " + page.getTotal());
        for (TestProduct p : page.getRecords()) {
            System.out.println("  " + p.getName() + " - ¥" + p.getPrice());
        }
        if (page.getRecords().size() != 2) {
            throw new AssertionError("分页结果不对");
        }

        // 9. Count
        System.out.println("\n========== 7. Count ==========");
        LambadaSelectWrapper<TestProduct> countWrapper = LambadaSelectWrapper.of();
        countWrapper.gt(TestProduct::getPrice, 3000);
        long count = productMapper.count(countWrapper);
        System.out.println("价格 > 3000 的商品数: " + count);
        if (count < 2) {
            throw new AssertionError("count不对");
        }

        // 10. Upsert 测试
        System.out.println("\n========== 8. Upsert ==========");
        // 第一次 upsert -> 插入
        TestProduct upsertP = new TestProduct();
        upsertP.setId(100L);
        upsertP.setName("Apple Watch");
        upsertP.setPrice(2999);
        upsertP.setCategory("手表");
        productMapper.upsert(upsertP);
        TestProduct found1 = productMapper.selectByPrimaryKey(100L);
        System.out.println("第一次Upsert（插入）: name=" + found1.getName());
        if (!"Apple Watch".equals(found1.getName())) {
            throw new AssertionError("第一次Upsert失败");
        }

        // 第二次 upsert -> 更新
        upsertP.setName("Apple Watch Ultra");
        upsertP.setPrice(6299);
        productMapper.upsert(upsertP);
        TestProduct found2 = productMapper.selectByPrimaryKey(100L);
        System.out.println("第二次Upsert（更新）: name=" + found2.getName() + ", price=" + found2.getPrice());
        if (!"Apple Watch Ultra".equals(found2.getName())) {
            throw new AssertionError("第二次Upsert更新失败");
        }
        if (found2.getPrice() != 6299) {
            throw new AssertionError("第二次Upsert价格不对");
        }

        // 11. 删除
        System.out.println("\n========== 9. 删除 ==========");
        int delRows = productMapper.deleteByPrimaryKey(p4.getId());
        System.out.println("删除影响行数: " + delRows);
        TestProduct deleted = productMapper.selectByPrimaryKey(p4.getId());
        if (deleted != null) {
            throw new AssertionError("删除后应该查不到");
        }

        // 12. 验证全部数据
        System.out.println("\n========== 10. 全部数据 ==========");
        LambadaSelectWrapper<TestProduct> allWrapper = LambadaSelectWrapper.of();
        allWrapper.orderByAsc(TestProduct::getId);
        List<TestProduct> all = productMapper.selectList(allWrapper);
        System.out.println("剩余商品数: " + all.size());
        for (TestProduct p : all) {
            System.out.println("  id=" + p.getId() + ", name=" + p.getName() + ", price=" + p.getPrice() + ", category=" + p.getCategory());
        }

        System.out.println("\n========================================");
        System.out.println("🎉 MapperFactory 动态代理测试全部通过！");
        System.out.println("========================================");

        ((HikariDataSource) dataSource).close();
    }
}
