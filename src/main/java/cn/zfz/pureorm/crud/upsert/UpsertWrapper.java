package cn.zfz.pureorm.crud.upsert;

import java.util.List;

import cn.zfz.pureorm.core.LambadaColumn;

public interface UpsertWrapper<W  extends UpsertWrapper<W,E>,E> {

	String getTableName();
	
	W insert(LambadaColumn<E> column,Object value);

	String getConflictKey();

	List<Object> getUpdateColumnValues();

	List<String> getUpdateColumnNames();

	List<Object> getInsertColumnValues();

	List<String> getInsertColumnNames();

}
