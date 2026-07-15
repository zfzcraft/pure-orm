package cn.zfz.pureorm.test.h2.rbac;

import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "sys_role_permission")
public class SysRolePermission {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private Long roleId;

    private Long permId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermId() { return permId; }
    public void setPermId(Long permId) { this.permId = permId; }
}
