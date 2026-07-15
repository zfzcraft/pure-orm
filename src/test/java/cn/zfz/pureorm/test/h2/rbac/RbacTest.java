package cn.zfz.pureorm.test.h2.rbac;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.zfz.pureorm.core.HighLevelMapper;
import cn.zfz.pureorm.core.MapperFactory;
import cn.zfz.pureorm.core.Page;
import cn.zfz.pureorm.crud.select.highlevel.HighLevelSelectWrapper;
import cn.zfz.pureorm.crud.select.single.LambadaSelectWrapper;
import cn.zfz.pureorm.crud.update.LambadaUpdateWrapper;

/**
 * RBAC 整体测试
 * 
 * 模型：
 *   sys_user          用户表
 *   sys_role          角色表
 *   sys_permission    权限表
 *   sys_user_role     用户-角色关联表
 *   sys_role_permission 角色-权限关联表
 * 
 * 测试内容：
 *   1. 建表 + 初始数据
 *   2. 单表 CRUD（MapperFactory 动态代理）
 *   3. Upsert
 *   4. 一对多关联查询：用户 -> 角色列表
 *   5. 一对多关联查询：角色 -> 权限列表
 *   6. 嵌套一对多关联查询：用户 -> 角色 -> 权限
 */
public class RbacTest {

    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:rbacdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        DataSource dataSource = new HikariDataSource(config);

        // ==================== 建表 ====================
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE sys_user (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50), " +
                    "password VARCHAR(200), " +
                    "status INT" +
                    ")");
            stmt.execute("CREATE TABLE sys_role (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "role_name VARCHAR(50), " +
                    "role_code VARCHAR(50), " +
                    "status INT" +
                    ")");
            stmt.execute("CREATE TABLE sys_permission (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "perm_name VARCHAR(50), " +
                    "perm_code VARCHAR(50), " +
                    "resource VARCHAR(200)" +
                    ")");
            stmt.execute("CREATE TABLE sys_user_role (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_id BIGINT, " +
                    "role_id BIGINT" +
                    ")");
            stmt.execute("CREATE TABLE sys_role_permission (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "role_id BIGINT, " +
                    "perm_id BIGINT" +
                    ")");
            System.out.println("✓ RBAC 5 张表创建成功");
        }

        // ==================== 创建 Mapper 代理 ====================
        SysUserMapper userMapper = MapperFactory.create(SysUserMapper.class, dataSource);
        SysRoleMapper roleMapper = MapperFactory.create(SysRoleMapper.class, dataSource);
        SysPermissionMapper permMapper = MapperFactory.create(SysPermissionMapper.class, dataSource);
        HighLevelMapper highLevelMapper = HighLevelMapper.of(dataSource);
        System.out.println("✓ Mapper 代理创建成功");

        // ==================== 初始化数据 ====================
        System.out.println("\n========== 初始化 RBAC 数据 ==========");

        // 用户
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword("123456");
        admin.setStatus(1);
        userMapper.insert(admin);

        SysUser zhangsan = new SysUser();
        zhangsan.setUsername("zhangsan");
        zhangsan.setPassword("zs123456");
        zhangsan.setStatus(1);
        userMapper.insert(zhangsan);

        SysUser lisi = new SysUser();
        lisi.setUsername("lisi");
        lisi.setPassword("ls123456");
        lisi.setStatus(0);
        userMapper.insert(lisi);
        System.out.println("用户: admin, zhangsan, lisi");

        // 角色
        SysRole roleAdmin = new SysRole();
        roleAdmin.setRoleName("管理员");
        roleAdmin.setRoleCode("ADMIN");
        roleAdmin.setStatus(1);
        roleMapper.insert(roleAdmin);

        SysRole roleUser = new SysRole();
        roleUser.setRoleName("普通用户");
        roleUser.setRoleCode("USER");
        roleUser.setStatus(1);
        roleMapper.insert(roleUser);

        SysRole roleGuest = new SysRole();
        roleGuest.setRoleName("访客");
        roleGuest.setRoleCode("GUEST");
        roleGuest.setStatus(0);
        roleMapper.insert(roleGuest);
        System.out.println("角色: 管理员, 普通用户, 访客");

        // 权限
        SysPermCreate("user:list", "查看用户列表", "/api/user/list", permMapper);
        SysPermCreate("user:add", "添加用户", "/api/user/add", permMapper);
        SysPermCreate("user:delete", "删除用户", "/api/user/delete", permMapper);
        SysPermCreate("order:list", "查看订单", "/api/order/list", permMapper);
        SysPermCreate("order:export", "导出订单", "/api/order/export", permMapper);
        SysPermCreate("dashboard:view", "查看仪表盘", "/api/dashboard", permMapper);
        System.out.println("权限: 6 个");

        // 用户-角色关联
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // admin -> 管理员 + 普通用户
            stmt.execute("INSERT INTO sys_user_role (user_id, role_id) VALUES (" + admin.getId() + ", " + roleAdmin.getId() + ")");
            stmt.execute("INSERT INTO sys_user_role (user_id, role_id) VALUES (" + admin.getId() + ", " + roleUser.getId() + ")");
            // zhangsan -> 普通用户
            stmt.execute("INSERT INTO sys_user_role (user_id, role_id) VALUES (" + zhangsan.getId() + ", " + roleUser.getId() + ")");
            // lisi -> 访客
            stmt.execute("INSERT INTO sys_user_role (user_id, role_id) VALUES (" + lisi.getId() + ", " + roleGuest.getId() + ")");

            // 角色-权限关联
            // 管理员 -> 全部权限
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 1)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 2)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 3)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 4)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 5)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleAdmin.getId() + ", 6)");
            // 普通用户 -> 查看权限
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleUser.getId() + ", 1)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleUser.getId() + ", 4)");
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleUser.getId() + ", 6)");
            // 访客 -> 只能看仪表盘
            stmt.execute("INSERT INTO sys_role_permission (role_id, perm_id) VALUES (" + roleGuest.getId() + ", 6)");
        }
        System.out.println("关联数据: 用户-角色, 角色-权限");

        // ==================== 1. 单表 CRUD ====================
        System.out.println("\n========== 1. 单表 CRUD ==========");

        // 查询
        LambadaSelectWrapper<SysUser> activeWrapper = LambadaSelectWrapper.of();
        activeWrapper.eq(SysUser::getStatus, 1);
        activeWrapper.orderByAsc(SysUser::getId);
        List<SysUser> activeUsers = userMapper.selectList(activeWrapper);
        System.out.println("状态=1 的用户: " + activeUsers.size() + " 个");
        if (activeUsers.size() != 2) {
            throw new AssertionError("应该有2个启用用户");
        }

        // 更新
        LambadaUpdateWrapper<SysRole> roleUpdate = LambadaUpdateWrapper.of();
        roleUpdate.set(SysRole::getStatus, 1);
        roleUpdate.eq(SysRole::getRoleCode, "GUEST");
        roleMapper.update(roleUpdate);
        SysRole updatedGuest = roleMapper.selectByPrimaryKey(roleGuest.getId());
        System.out.println("访客状态更新为: " + updatedGuest.getStatus());
        if (updatedGuest.getStatus() != 1) {
            throw new AssertionError("更新失败");
        }

        // 分页
        LambadaSelectWrapper<SysPermission> permPageWrapper = LambadaSelectWrapper.of();
        permPageWrapper.orderByAsc(SysPermission::getId);
        Page<SysPermission> permPage = permMapper.selectPage(permPageWrapper, 1, 3);
        System.out.println("权限分页(第1页, 每页3条): " + permPage.getRecords().size() + " 条, 总数: " + permPage.getTotal());
        if (permPage.getRecords().size() != 3 || permPage.getTotal() != 6) {
            throw new AssertionError("分页不对");
        }

        // Upsert
        SysUser upsertUser = new SysUser();
        upsertUser.setId(200L);
        upsertUser.setUsername("upsert_user");
        upsertUser.setPassword("pwd");
        upsertUser.setStatus(1);
        userMapper.upsert(upsertUser);
        upsertUser.setStatus(0);
        userMapper.upsert(upsertUser);
        SysUser upsertFound = userMapper.selectByPrimaryKey(200L);
        System.out.println("Upsert 用户: username=" + upsertFound.getUsername() + ", status=" + upsertFound.getStatus());
        if (upsertFound.getStatus() != 0) {
            throw new AssertionError("Upsert 更新失败");
        }

        // ==================== 2. 一对多：用户 -> 角色列表 ====================
        System.out.println("\n========== 2. 一对多关联查询：用户 -> 角色列表 ==========");
        List<UserWithRoles> userRolesResult = highLevelMapper.selectList(
                HighLevelSelectWrapper.of(SysUser.class)
                        .select(SysUser::getId, SysUser::getUsername, SysUser::getStatus)
                        .leftJoin(SysUserRole.class, on -> on.eq(SysUser::getId, SysUserRole::getUserId))
                        .leftJoin(SysRole.class, on -> on.eq(SysUserRole::getRoleId, SysRole::getId))
                        .select(SysRole::getId, SysRole::getRoleName, SysRole::getRoleCode)
                        .orderByAsc(SysUser::getId),
                UserWithRoles.class
        );

        for (UserWithRoles u : userRolesResult) {
            System.out.println("用户: " + u.getUsername() + " (id=" + u.getId() + ")");
            if (u.getRoles() != null) {
                for (SysRole r : u.getRoles()) {
                    System.out.println("  -> 角色: " + r.getRoleName() + " (" + r.getRoleCode() + ")");
                }
            }
        }

        // admin 应该有 2 个角色
        UserWithRoles adminWithRoles = userRolesResult.stream()
                .filter(u -> "admin".equals(u.getUsername()))
                .findFirst().orElse(null);
        if (adminWithRoles == null || adminWithRoles.getRoles() == null || adminWithRoles.getRoles().size() != 2) {
            throw new AssertionError("admin 应该有2个角色");
        }

        // ==================== 3. 一对多：角色 -> 权限列表 ====================
        System.out.println("\n========== 3. 一对多关联查询：角色 -> 权限列表 ==========");
        List<RoleWithPermissions> rolePermResult = highLevelMapper.selectList(
                HighLevelSelectWrapper.of(SysRole.class)
                        .select(SysRole::getId, SysRole::getRoleName, SysRole::getRoleCode)
                        .leftJoin(SysRolePermission.class, on -> on.eq(SysRole::getId, SysRolePermission::getRoleId))
                        .leftJoin(SysPermission.class, on -> on.eq(SysRolePermission::getPermId, SysPermission::getId))
                        .select(SysPermission::getId, SysPermission::getPermName, SysPermission::getPermCode)
                        .orderByAsc(SysRole::getId)
                        .orderByAsc(SysPermission::getId),
                RoleWithPermissions.class
        );

        for (RoleWithPermissions r : rolePermResult) {
            System.out.println("角色: " + r.getRoleName() + " (" + r.getRoleCode() + ")");
            if (r.getPermissions() != null) {
                for (SysPermission p : r.getPermissions()) {
                    System.out.println("  -> 权限: " + p.getPermName() + " (" + p.getPermCode() + ")");
                }
            }
        }

        // 管理员角色应该有 6 个权限
        RoleWithPermissions adminRole = rolePermResult.stream()
                .filter(r -> "ADMIN".equals(r.getRoleCode()))
                .findFirst().orElse(null);
        if (adminRole == null || adminRole.getPermissions() == null || adminRole.getPermissions().size() != 6) {
            throw new AssertionError("管理员角色应该有6个权限");
        }

        // 普通用户角色应该有 3 个权限
        RoleWithPermissions userRole = rolePermResult.stream()
                .filter(r -> "USER".equals(r.getRoleCode()))
                .findFirst().orElse(null);
        if (userRole == null || userRole.getPermissions() == null || userRole.getPermissions().size() != 3) {
            throw new AssertionError("普通用户角色应该有3个权限");
        }

        // ==================== 4. 嵌套一对多：用户 -> 角色 -> 权限 ====================
        System.out.println("\n========== 4. 嵌套关联查询：用户 -> 角色 -> 权限 ==========");
        List<UserWithRolesAndPermissions> nestedResult = highLevelMapper.selectList(
                HighLevelSelectWrapper.of(SysUser.class)
                        .select(SysUser::getId, SysUser::getUsername)
                        .leftJoin(SysUserRole.class, on -> on.eq(SysUser::getId, SysUserRole::getUserId))
                        .leftJoin(SysRole.class, on -> on.eq(SysUserRole::getRoleId, SysRole::getId))
                        .select(SysRole::getId, SysRole::getRoleName, SysRole::getRoleCode)
                        .leftJoin(SysRolePermission.class, on -> on.eq(SysRole::getId, SysRolePermission::getRoleId))
                        .leftJoin(SysPermission.class, on -> on.eq(SysRolePermission::getPermId, SysPermission::getId))
                        .select(SysPermission::getId, SysPermission::getPermName, SysPermission::getPermCode)
                        .eq(SysUser::getId, admin.getId())
                        .orderByAsc(SysRole::getId)
                        .orderByAsc(SysPermission::getId),
                UserWithRolesAndPermissions.class
        );

        System.out.println("查询 admin 的角色和权限:");
        for (UserWithRolesAndPermissions u : nestedResult) {
            System.out.println("用户: " + u.getUsername() + " (id=" + u.getId() + ")");
            if (u.getRoles() != null) {
                for (SysRole r : u.getRoles()) {
                    System.out.println("  角色: " + r.getRoleName() + " (" + r.getRoleCode() + ")");
                    if (r.getPermissions() != null) {
                        for (SysPermission p : r.getPermissions()) {
                            System.out.println("    -> 权限: " + p.getPermName() + " (" + p.getPermCode() + ")");
                        }
                    }
                }
            }
        }

        if (nestedResult.size() != 1) {
            throw new AssertionError("应该只查到1个用户");
        }
        UserWithRolesAndPermissions adminNested = nestedResult.get(0);
        if (adminNested.getRoles() == null || adminNested.getRoles().size() != 2) {
            throw new AssertionError("admin 应该有2个角色");
        }
        // 管理员角色有6个权限
        SysRole adminRoleNested = adminNested.getRoles().stream()
                .filter(r -> "ADMIN".equals(r.getRoleCode()))
                .findFirst().orElse(null);
        if (adminRoleNested == null || adminRoleNested.getPermissions() == null || adminRoleNested.getPermissions().size() != 6) {
            throw new AssertionError("管理员角色应该有6个权限");
        }
        // 普通用户角色有3个权限
        SysRole userRoleNested = adminNested.getRoles().stream()
                .filter(r -> "USER".equals(r.getRoleCode()))
                .findFirst().orElse(null);
        if (userRoleNested == null || userRoleNested.getPermissions() == null || userRoleNested.getPermissions().size() != 3) {
            throw new AssertionError("普通用户角色应该有3个权限");
        }

        System.out.println("\n========================================");
        System.out.println("🎉 RBAC 整体测试全部通过！");
        System.out.println("========================================");
        System.out.println("测试覆盖:");
        System.out.println("  ✅ 单表 CRUD（MapperFactory 动态代理）");
        System.out.println("  ✅ 条件查询 + 排序 + 分页");
        System.out.println("  ✅ Upsert（存在则更新，不存在则插入）");
        System.out.println("  ✅ 一对多关联查询：用户 -> 角色列表");
        System.out.println("  ✅ 一对多关联查询：角色 -> 权限列表");
        System.out.println("  ✅ 嵌套一对多：用户 -> 角色 -> 权限");

        ((HikariDataSource) dataSource).close();
    }

    private static void SysPermCreate(String code, String name, String resource, SysPermissionMapper mapper) {
        SysPermission perm = new SysPermission();
        perm.setPermCode(code);
        perm.setPermName(name);
        perm.setResource(resource);
        mapper.insert(perm);
    }
}
