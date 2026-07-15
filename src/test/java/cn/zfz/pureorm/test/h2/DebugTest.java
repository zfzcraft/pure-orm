package cn.zfz.pureorm.test.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.HighLevelMapper;
import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.select.highlevel.HighLevelSelectWrapper;
import cn.zfz.pureorm.crud.select.highlevel.HighLevelSqlGenerator;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.dialect.DialectFactory;
import cn.zfz.pureorm.enums.DbType;

public class DebugTest {
    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
            stmt.execute("INSERT INTO t_user (name, age, email) VALUES ('订单用户', 30, 'a@b.com')");
            stmt.execute("INSERT INTO t_order (user_id, order_no, amount) VALUES (1, 'ORDER001', 100)");
            stmt.execute("INSERT INTO t_order (user_id, order_no, amount) VALUES (1, 'ORDER002', 200)");
            stmt.execute("INSERT INTO t_order (user_id, order_no, amount) VALUES (1, 'ORDER003', 300)");
        }

        try (Connection conn = dataSource.getConnection()) {
            DbType dbType = DbType.H2;
            Dialect dialect = DialectFactory.detect(dbType);
            HighLevelSelectWrapper wrapper = HighLevelSelectWrapper.of(TestUser.class);
            wrapper.select(TestUser::getId, TestUser::getName, TestUser::getAge);
            wrapper.leftJoin(TestOrder.class, on -> on.eq(TestUser::getId, TestOrder::getUserId));
            wrapper.selectAll(TestOrder.class);
            wrapper.eq(TestUser::getId, 1L);
            wrapper.orderByAsc(TestOrder::getId);

            SqlAndParams sqlAndParams = HighLevelSqlGenerator.buildSql(wrapper, dialect);
            System.out.println("生成的 SQL:");
            System.out.println(sqlAndParams.getSql());
            System.out.println("参数: " + sqlAndParams.getParams());

            PreparedStatement pstmt = conn.prepareStatement(sqlAndParams.getSql());
            for (int i = 0; i < sqlAndParams.getParams().size(); i++) {
                pstmt.setObject(i + 1, sqlAndParams.getParams().get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            System.out.println("\nResultSet 列:");
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println("  " + i + ": " + md.getColumnLabel(i));
            }
            System.out.println("\n数据行:");
            int rowNum = 0;
            while (rs.next()) {
                rowNum++;
                System.out.println("行 " + rowNum + ":");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("  " + md.getColumnLabel(i) + " = " + rs.getObject(i));
                }
            }
            System.out.println("总行数: " + rowNum);
        }

        HighLevelMapper highLevelMapper = HighLevelMapper.of(dataSource);
        List<TestUserWithOrders> result = highLevelMapper.selectList(
                HighLevelSelectWrapper.of(TestUser.class)
                        .select(TestUser::getId, TestUser::getName, TestUser::getAge)
                        .leftJoin(TestOrder.class, on -> on.eq(TestUser::getId, TestOrder::getUserId))
                        .selectAll(TestOrder.class)
                        .eq(TestUser::getId, 1L)
                        .orderByAsc(TestOrder::getId),
                TestUserWithOrders.class
        );
        System.out.println("\n映射结果数量: " + result.size());
        for (TestUserWithOrders u : result) {
            System.out.println("用户: " + u.getName() + ", 订单数: " + (u.getOrders() == null ? "null" : u.getOrders().size()));
            if (u.getOrders() != null) {
                for (TestOrder o : u.getOrders()) {
                    System.out.println("  订单: " + o.getOrderNo() + ", 金额: " + o.getAmount());
                }
            }
        }

        ((HikariDataSource) dataSource).close();
    }
}
