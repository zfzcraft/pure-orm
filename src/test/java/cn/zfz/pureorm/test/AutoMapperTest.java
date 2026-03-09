package cn.zfz.pureorm.test;

import java.util.List;

import cn.zfz.pureorm.core.AutoEntityMapper;
import cn.zfz.pureorm.test.insert.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AutoMapperTest {
	private static final String URL = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false";
	private static final String USER = "root";
	private static final String PASSWORD = "root";

	public static void main(String[] args) throws Exception {

		try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
			// 1. 执行关联查询，返回带前缀的列名
			String sql = "SELECT " + "u.name AS `user.name`, u.age AS `user.age`, "+ "u.id AS `user.id`, "
					+ "r.code AS `role.code`, r.name AS `role.name`, "+ "r.id AS `role.id`,  "
					+ "p.id AS `permission.id`,"	+ "p.url AS `permission.url`, p.desc AS `permission.desc` " + "FROM user u "
					+ "JOIN user_role ur ON u.id = ur.user_id " + "JOIN role r ON ur.role_id = r.id "
					+ "JOIN role_permission rp ON r.id = rp.role_id " + "JOIN permission p ON rp.permission_id = p.id "
					+ "LIMIT 100";

			try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

				// 2. 调用buildGroupMap（你最终确定的版本）
				List<User> userList = AutoEntityMapper.map(rs, User.class);
				for (User user : userList) {
					System.out.println("========================");
					System.out.println("用户：" + user);
					
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}