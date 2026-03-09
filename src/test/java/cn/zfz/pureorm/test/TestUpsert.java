//package cn.pureorm.test;
//
//import javax.sql.DataSource;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import cn.pureorm.core.MapperFactory;
//import cn.pureorm.crud.upsert.UpsertSqlBuilder;
//import cn.pureorm.crud.upsert.UpsertWrapper;
//import cn.pureorm.dialect.MySQLDialect;
//import cn.pureorm.test.insert.UserMapper;
//
//public class TestUpsert {
//
//	public static void main(String[] args) {
//		DataSource dataSource = getHikariDataSource();
//		User22 user = new User22();
//		user.setId(10);
//		user.setGender(Gender.FEMALE);
//		user.setName("DOVA_UPSERT22222");
//		user.setPhne("1234567892222");
//		user.setUserStatus(UserStatus.LOCKED);
//		UpsertWrapper<User22> upsertWrapper = new UpsertWrapper<>(user);
//		
//		System.out.println(UpsertSqlBuilder.buildSql(upsertWrapper, new MySQLDialect()));
//		
//		UserMapper userMapper = MapperFactory.create(UserMapper.class, dataSource);
//		System.out.println(userMapper.upsert(user));
//	}
//	
//	public static DataSource getHikariDataSource() {
//		HikariConfig config = new HikariConfig();
//		// 数据库基础配置
//		config.setJdbcUrl("jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Shanghai");
//		config.setUsername("root");
//		config.setPassword("root");
//		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
//		// 连接池核心配置
//		config.setMaximumPoolSize(20);
//		config.setMinimumIdle(5);
//		config.setConnectionTimeout(30000);
//		config.setPoolName("TestHikariPool");
//		// 创建并返回数据源
//		return new HikariDataSource(config);
//	}
//}



