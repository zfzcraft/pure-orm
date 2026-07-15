package cn.zfz.pureorm.core;

import java.io.Serializable;
import java.util.List;

import cn.zfz.pureorm.crud.delete.LambadaDeleteWrapper;
import cn.zfz.pureorm.crud.insert.InsertWrapper;
import cn.zfz.pureorm.crud.select.single.SelectWrapper;
import cn.zfz.pureorm.crud.update.UpdateWrapper;
import cn.zfz.pureorm.crud.upsert.LambadaUpsertWrapper;

public interface BaseMapper<T> {
	
	<W extends InsertWrapper<W, T>> long insert(InsertWrapper<W, T> insertWrapper);

	long insert(T entity);

	long insertNotNull(T entity);

	List<Long> insertBatch(List<T> entities);

	List<Long> insertBatch(List<T> entities, int batchSize);

	<W extends UpdateWrapper<W, T>> int update(W updateWrapper);

	int updateByPrimaryKey(T entity);

	int updateNotNullByPrimaryKey(T entity);

	int delete(LambadaDeleteWrapper<T> deleteWrapper);

	int deleteByPrimaryKey(Serializable primaryKey);

	int deleteByPrimaryKeys(List<Serializable> primaryKeys);

	<W extends SelectWrapper<W, T>> T selectOne(W selectWrapper);

	T selectByPrimaryKey(Serializable primaryKey);

	List<T> selectByPrimaryKeyList(List<Serializable> primaryKeys);

	<W extends SelectWrapper<W, T>> List<T> selectList(W selectWrapper);

	<W extends SelectWrapper<W, T>> long count(W selectWrapper);

	<W extends SelectWrapper<W, T>> boolean exists(W selectWrapper);

	<W extends SelectWrapper<W, T>> Page<T> selectPage(W selectWrapper, int pageNum, int pageSize);

	long upsert(T entity);

	long upsertNotNull(T entity);

	int upsert(LambadaUpsertWrapper<T> upsertWrapper);

}
