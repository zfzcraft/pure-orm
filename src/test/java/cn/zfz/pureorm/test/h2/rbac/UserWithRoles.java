package cn.zfz.pureorm.test.h2.rbac;

import java.util.List;

import cn.zfz.pureorm.annotations.NotColumn;

/**
 * 用户 + 角色列表（一对多关联查询结果）
 * 查询 SQL 别名前缀：sysuser.id, sysuser.username, sysrole.id, sysrole.role_name ...
 */
public class UserWithRoles {

    private Long id;

    private String username;

    private String password;

    private Integer status;

    // 一对多：用户拥有的角色
    @NotColumn
    private List<SysRole> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<SysRole> getRoles() { return roles; }
    public void setRoles(List<SysRole> roles) { this.roles = roles; }
}
