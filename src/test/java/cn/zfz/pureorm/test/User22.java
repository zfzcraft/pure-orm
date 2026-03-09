package cn.zfz.pureorm.test;

import cn.zfz.pureorm.annotations.Column;
import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.OneToMany;
import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;
import lombok.Data;
@Data
@Table(name = "t_user")
public class User22 {
	@PrimaryKey(autoIncrement = true)
	private Integer id;
	
	@Column(name = "phone")
	private String phne;
	
	private String name;
	
	@NotColumn
	private String ignore;
	
	private Gender gender;
	
	private UserStatus userStatus;

}
