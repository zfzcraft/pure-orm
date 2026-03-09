package cn.zfz.pureorm.test.insert;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.MapperFactory;
import cn.zfz.pureorm.crud.insert.LambadaInsertWrapper;

public class InsertWrapperTest {
	public static void main(String[] args) {
		DataSource dataSource = getHikariDataSource();
		UserMapper userMapper = MapperFactory.create(UserMapper.class, dataSource);
		
		LambadaInsertWrapper<User> insertWrapper = LambadaInsertWrapper.of();
		insertWrapper.insert(User::getId, 16);
		insertWrapper.insert(User::getName, "milk");
		insertWrapper.insert(User::getAge, 21);
		userMapper.insert(insertWrapper);
		System.out.println(insertWrapper);
		
		User user = new User();
		user.setId(17L);
		user.setName("dova");
		user.setAge(18);
		userMapper.insert(user);
	}

	public static DataSource getHikariDataSource() {
		HikariConfig config = new HikariConfig();
		// 数据库基础配置
		config.setJdbcUrl("jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Shanghai");
		config.setUsername("root");
		config.setPassword("root");
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		// 连接池核心配置
		config.setMaximumPoolSize(20);
		config.setMinimumIdle(5);
		config.setConnectionTimeout(30000);
		config.setPoolName("TestHikariPool");
		// 创建并返回数据源
		return new HikariDataSource(config);
	}
}
