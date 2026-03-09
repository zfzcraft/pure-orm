package cn.zfz.pureorm.crud.select.natives;

import java.util.List;

import cn.zfz.pureorm.core.LambadaColumn;

public interface NativeSelectWrapper<W extends NativeSelectWrapper<W,E>, E> {

	W nativeSQL(String sql);
	
	W nativeSQL(String sql,Object[] params);
	
	W mapping(String column,LambadaColumn<E> field);

	List<ColumnMapping> getColumnMappings();

	Object[] getParams();

	String getNativeSQL();
}
