package cn.zfz.pureorm.test.h2;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.BaseMapper;
import cn.zfz.pureorm.core.BaseMapperExecutor;
import cn.zfz.pureorm.core.HighLevelMapper;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.Page;
import cn.zfz.pureorm.crud.delete.LambadaDeleteWrapper;
import cn.zfz.pureorm.crud.select.highlevel.HighLevelSelectWrapper;
import cn.zfz.pureorm.crud.select.single.LambadaSelectWrapper;
import cn.zfz.pureorm.crud.update.LambadaUpdateWrapper;

public class PureOrmH2TestRunner {

    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        DataSource dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE t_user (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100), " +
                    "age INT, " +
                    "email VARCHAR(200)" +
                    ")");
            stmt.execute("CREATE TABLE t_order (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_id BIGINT, " +
                    "order_no VARCHAR(100), " +
                    "amount INT" +
                    ")");
            System.out.println("✓ 表创建成功");
        }

        BaseMapper<TestUser> userMapper = new BaseMapperExecutor<>(dataSource, TestUser.class);

        System.out.println("\n========== 1. 测试插入 ==========");
        TestUser user = new TestUser();
        user.setName("张三");
        user.setAge(25);
        user.setEmail("zhangsan@example.com");
        long rows = userMapper.insert(user);
        System.out.println("插入成功，影响行数: " + rows + ", 自增ID: " + user.getId());
        if (user.getId() == null) {
            throw new AssertionError("ID 不应该为空");
        }

        System.out.println("\n========== 2. 测试根据ID查询 ==========");
        TestUser found = userMapper.selectByPrimaryKey(user.getId());
        System.out.println("查询结果: id=" + found.getId() + ", name=" + found.getName() + ", age=" + found.getAge());
        if (!"张三".equals(found.getName())) {
            throw new AssertionError("姓名不匹配");
        }
        if (found.getAge() != 25) {
            throw new AssertionError("年龄不匹配");
        }

        System.out.println("\n========== 3. 测试 updateById ==========");
        user.setName("张三四");
        user.setAge(26);
        int updateRows = userMapper.updateByPrimaryKey(user);
        System.out.println("更新影响行数: " + updateRows);
        TestUser updated = userMapper.selectByPrimaryKey(user.getId());
        System.out.println("更新后: name=" + updated.getName() + ", age=" + updated.getAge());
        if (!"张三四".equals(updated.getName())) {
            throw new AssertionError("更新后姓名不匹配");
        }

        System.out.println("\n========== 4. 测试 LambdaUpdateWrapper ==========");
        LambadaUpdateWrapper<TestUser> updateWrapper = LambadaUpdateWrapper.of();
        updateWrapper.set(TestUser::getAge, 28);
        updateWrapper.eq(TestUser::getId, user.getId());
        int lambdaUpdateRows = userMapper.update(updateWrapper);
        System.out.println("Lambda更新影响行数: " + lambdaUpdateRows);
        TestUser lambdaUpdated = userMapper.selectByPrimaryKey(user.getId());
        System.out.println("Lambda更新后 age=" + lambdaUpdated.getAge());
        if (lambdaUpdated.getAge() != 28) {
            throw new AssertionError("Lambda更新失败");
        }

        System.out.println("\n========== 5. 测试批量插入 ==========");
        for (int i = 1; i <= 5; i++) {
            TestUser u = new TestUser();
            u.setName("用户" + i);
            u.setAge(20 + i);
            u.setEmail("user" + i + "@test.com");
            userMapper.insert(u);
        }
        System.out.println("批量插入完成");

        System.out.println("\n========== 6. 测试 LambadaSelectWrapper 条件查询 ==========");
        LambadaSelectWrapper<TestUser> selectWrapper = LambadaSelectWrapper.of();
        selectWrapper.gt(TestUser::getAge, 22);
        List<TestUser> list = userMapper.selectList(selectWrapper);
        System.out.println("条件查询结果数量: " + list.size());
        for (TestUser u : list) {
            System.out.println("  id=" + u.getId() + ", name=" + u.getName() + ", age=" + u.getAge());
        }
        if (list.size() < 3) {
            throw new AssertionError("条件查询结果数量不对");
        }

        System.out.println("\n========== 7. 测试排序 ==========");
        LambadaSelectWrapper<TestUser> orderWrapper = LambadaSelectWrapper.of();
        orderWrapper.orderByAsc(TestUser::getAge);
        List<TestUser> ascList = userMapper.selectList(orderWrapper);
        System.out.println("按年龄升序（前3条）:");
        for (int i = 0; i < Math.min(3, ascList.size()); i++) {
            System.out.println("  age=" + ascList.get(i).getAge());
        }
        if (ascList.get(0).getAge() > ascList.get(1).getAge()) {
            throw new AssertionError("排序不对");
        }

        System.out.println("\n========== 8. 测试分页 ==========");
        LambadaSelectWrapper<TestUser> pageWrapper = LambadaSelectWrapper.of();
        pageWrapper.orderByAsc(TestUser::getId);
        Page<TestUser> page = userMapper.selectPage(pageWrapper, 1, 3);
        System.out.println("分页查询（第1页，每页3条）: " + page.getRecords().size() + " 条");
        System.out.println("总记录数: " + page.getTotal());
        if (page.getRecords().size() != 3) {
            throw new AssertionError("分页结果数量不对");
        }

        System.out.println("\n========== 9. 测试 Count ==========");
        LambadaSelectWrapper<TestUser> countWrapper = LambadaSelectWrapper.of();
        countWrapper.gt(TestUser::getAge, 20);
        long count = userMapper.count(countWrapper);
        System.out.println("年龄大于20的用户数: " + count);
        if (count < 5) {
            throw new AssertionError("count 不对");
        }

        System.out.println("\n========== 10. 测试删除 ==========");
        Long deleteId = user.getId();
        int deleteRows = userMapper.deleteByPrimaryKey(deleteId);
        System.out.println("删除影响行数: " + deleteRows);
        TestUser deleted = userMapper.selectByPrimaryKey(deleteId);
        if (deleted != null) {
            throw new AssertionError("删除后应该查不到");
        }
        System.out.println("删除后验证通过");

        System.out.println("\n========== 11. 测试 LambdaDeleteWrapper ==========");
        TestUser delUser = new TestUser();
        delUser.setName("待删除用户");
        delUser.setAge(99);
        userMapper.insert(delUser);
        LambadaDeleteWrapper<TestUser> delWrapper = LambadaDeleteWrapper.of();
        delWrapper.eq(TestUser::getAge, 99);
        int lambdaDelRows = userMapper.delete(delWrapper);
        System.out.println("Lambda删除影响行数: " + lambdaDelRows);
        if (lambdaDelRows < 1) {
            throw new AssertionError("Lambda删除失败");
        }

        System.out.println("\n========== 12. 测试高级关联查询（Lambda + 自动映射）==========");
        TestUser orderUser = new TestUser();
        orderUser.setName("订单用户");
        orderUser.setAge(30);
        userMapper.insert(orderUser);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO t_order (user_id, order_no, amount) VALUES " +
                    "(" + orderUser.getId() + ", 'ORDER001', 100), " +
                    "(" + orderUser.getId() + ", 'ORDER002', 200), " +
                    "(" + orderUser.getId() + ", 'ORDER003', 300)");
        }

        HighLevelMapper highLevelMapper = HighLevelMapper.of(dataSource);
        List<TestUserWithOrders> result = highLevelMapper.selectList(
                HighLevelSelectWrapper.of(TestUser.class)
                        .select(TestUser::getId, TestUser::getName, TestUser::getAge)
                        .leftJoin(TestOrder.class, on -> on.eq(TestUser::getId, TestOrder::getUserId))
                        .selectAll(TestOrder.class)
                        .eq(TestUser::getId, orderUser.getId())
                        .orderByAsc(TestOrder::getId),
                TestUserWithOrders.class
        );

        System.out.println("关联查询结果数量: " + result.size());
        if (result.size() != 1) {
            throw new AssertionError("应该只有1个用户");
        }
        TestUserWithOrders uo = result.get(0);
        System.out.println("用户: " + uo.getName());
        System.out.println("订单数量: " + uo.getOrders().size());
        if (uo.getOrders() == null || uo.getOrders().size() != 3) {
            throw new AssertionError("应该有3个订单, 实际: " + (uo.getOrders() == null ? "null" : uo.getOrders().size()));
        }
        for (TestOrder o : uo.getOrders()) {
            System.out.println("  订单: " + o.getOrderNo() + ", 金额: " + o.getAmount());
        }
        if (!"ORDER001".equals(uo.getOrders().get(0).getOrderNo())) {
            throw new AssertionError("第一个订单应该是 ORDER001");
        }

        System.out.println("\n========== 13. 测试 Upsert（存在则更新，不存在则插入）==========");
        TestUser upsertUser = new TestUser();
        upsertUser.setId(100L);
        upsertUser.setName("Upsert测试");
        upsertUser.setAge(20);
        upsertUser.setEmail("upsert@test.com");
        long upsertRows1 = userMapper.upsert(upsertUser);
        System.out.println("第一次Upsert（插入）影响行数: " + upsertRows1);
        TestUser found1 = userMapper.selectByPrimaryKey(100L);
        System.out.println("查询: name=" + found1.getName() + ", age=" + found1.getAge());
        if (!"Upsert测试".equals(found1.getName())) {
            throw new AssertionError("第一次Upsert后姓名不对");
        }

        upsertUser.setName("Upsert已更新");
        upsertUser.setAge(25);
        long upsertRows2 = userMapper.upsert(upsertUser);
        System.out.println("第二次Upsert（更新）影响行数: " + upsertRows2);
        TestUser found2 = userMapper.selectByPrimaryKey(100L);
        System.out.println("查询: name=" + found2.getName() + ", age=" + found2.getAge());
        if (!"Upsert已更新".equals(found2.getName())) {
            throw new AssertionError("第二次Upsert后姓名不对");
        }
        if (found2.getAge() != 25) {
            throw new AssertionError("第二次Upsert后年龄不对");
        }

        System.out.println("\n========== 14. 测试缓存性能 ==========");
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            LambadaSelectWrapper<TestUser> w = LambadaSelectWrapper.of();
            w.eq(TestUser::getName, "test");
            w.gt(TestUser::getAge, 18);
            w.orderByAsc(TestUser::getId);
            userMapper.selectList(w);
        }
        long end = System.nanoTime();
        System.out.println("10000 次 Lambda 查询耗时: " + (end - start) / 1000000 + "ms");
        System.out.println("（缓存生效，每次查询无需重复解析 Lambda）");

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");

        ((HikariDataSource) dataSource).close();
    }
}
