package cn.zfz.pureorm.test;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.MapperFactory;
import cn.zfz.pureorm.test.insert.UserMapper;

public class TestDelete {
	public static void main(String[] args) {
		DataSource dataSource = getHikariDataSource();
		UserMapper userMapper = MapperFactory.create(UserMapper.class, dataSource);
		int res = userMapper.deleteByPrimaryKey(2);

		System.out.println(res);
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
