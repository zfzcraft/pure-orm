//package cn.pureorm.test;
//
//import javax.sql.DataSource;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import cn.pureorm.core.MapperFactory;
//import cn.pureorm.crud.select.SelectWrapper;
//import cn.pureorm.test.insert.UserMapper;
//
//public class TestSelect {
//	public static void main(String[] args) {
//		DataSource dataSource = getHikariDataSource();
//		//testPrimaryKey(dataSource);
//		UserMapper userMapper = MapperFactory.create(UserMapper.class, dataSource);
//		SelectWrapper<User22> selectWrapper = new SelectWrapper<>();
//		selectWrapper.selectAll().ge("id", 7);
//		System.out.println(userMapper.select(selectWrapper));
// 	}
//
//	@SuppressWarnings("unused")
//	private static void testPrimaryKey(DataSource dataSource) {
//		UserMapper userMapper = MapperFactory.create(UserMapper.class, dataSource);
//		User22 user = userMapper.selectByPrimaryKey(9);
//
//		System.out.println(user);
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



