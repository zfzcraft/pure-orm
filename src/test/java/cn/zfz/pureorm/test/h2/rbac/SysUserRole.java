package cn.zfz.pureorm.test.h2.rbac;

import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "sys_user_role")
public class SysUserRole {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private Long userId;

    private Long roleId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
