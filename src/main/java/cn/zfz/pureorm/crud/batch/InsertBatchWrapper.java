package cn.zfz.pureorm.crud.batch;

import java.util.*;

public interface InsertBatchWrapper<W extends InsertBatchWrapper<W, E>,E> {

	String getTableName();

	List<String> getColumnNames();

	List<List<Object>> getColumnValuesList();

}