package cn.zfz.pureorm.test.h2.rbac;

import java.util.List;

import cn.zfz.pureorm.annotations.NotColumn;

/**
 * 角色 + 权限列表（一对多关联查询结果）
 */
public class RoleWithPermissions {

    private Long id;

    private String roleName;

    private String roleCode;

    private Integer status;

    // 一对多：角色拥有的权限
    @NotColumn
    private List<SysPermission> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<SysPermission> getPermissions() { return permissions; }
    public void setPermissions(List<SysPermission> permissions) { this.permissions = permissions; }
}
