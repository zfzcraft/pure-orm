package cn.zfz.pureorm.crud.insert;

import java.util.List;

import cn.zfz.pureorm.core.LambadaColumn;

public interface InsertWrapper<W extends InsertWrapper<W, E>, E> {

	String getTableName();

	W insert(LambadaColumn<E> field, Object value);

	List<String> getColumnNames();

	List<Object> getColumnValues();

}
