// com.pureorm.core.BaseMapper.java
package cn.zfz.pureorm.core;

import java.io.Serializable;
import java.util.List;

import cn.zfz.pureorm.crud.delete.LambadaDeleteWrapper;
import cn.zfz.pureorm.crud.insert.InsertWrapper;
import cn.zfz.pureorm.crud.select.UpdateWrapper;
import cn.zfz.pureorm.crud.select.single.SelectWrapper;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

/**
 * PureORM 基础 Mapper 接口。
 * <p>
 * 设计原则： - 所有写操作必须带 WHERE 条件（防止全表更新/删除） - 所有查询通过 Wrapper 构造，确保编译期安全 - 不提供
 * save()/saveOrUpdate() 等模糊语义方法 - 不管理事务、连接、缓存
 *
 */
public interface BaseMapper<T> {
	/**
	 * 
	 * @param entity
	 * @return generated key or affected row
	 */
	long insert(T entity);

	long insertNotNull(T entity);

	<W extends InsertWrapper<W, T>> long insert(InsertWrapper<W, T> insertWrapper);

	List<Long> insertBatch(List<T> entities);

	List<Long> insertBatch(List<T> entities, int batchSize);

	/**
	 * 
	 * @param entity
	 * @return affected row
	 */
	int upsert(T entity);

	int upsertNotNull(T entity);

	<W extends UpsertWrapper<W, T>> int upsert(UpsertWrapper<W, T> upsertWrapper);

	/**
	 * 
	 * @param entities
	 * @return affected rows
	 */
	List<Integer> upsertBatch(List<T> entities);

	List<Integer> upsertBatch(List<T> entities, int batchSize);

	
	/**
	 * 
	 * @param updateWrapper
	 * @return affected row
	 */
	<W extends UpdateWrapper<W, T>> int update(UpdateWrapper<W, T> updateWrapper);

	/**
	 * 
	 * @param updateWrappers
	 * @return affected rows
	 */
	<W extends UpdateWrapper<W, T>> List<Integer> batchUpdate(List<UpdateWrapper<W, T>> updateWrappers);

	<W extends UpdateWrapper<W, T>> List<Integer> batchUpdate(List<UpdateWrapper<W, T>> updateWrappers, int batchSize);

	/**
	 * 
	 * @param deleteWrapper
	 * @return affected row
	 */
	int delete(LambadaDeleteWrapper<T> deleteWrapper);

	int deleteByPrimaryKey(Serializable primaryKey);

	List<Integer> deleteByPrimaryKeys(List<Serializable> primaryKeys);

	
	<W extends SelectWrapper<W, T>> T selectOne(SelectWrapper<W, T> selectWrapper);

	T selectByPrimaryKey(Serializable primaryKey);

	List<T> selectByPrimaryKeyList(List<Serializable> primaryKeys);

	<W extends SelectWrapper<W, T>> List<T> selectList(SelectWrapper<W, T> selectWrapper);

	<W extends SelectWrapper<W, T>> long count(SelectWrapper<W, T> selectWrapper);

	<W extends SelectWrapper<W, T>> boolean exists(SelectWrapper<W, T> selectWrapper);

	<W extends SelectWrapper<W, T>> Page<T> selectPage(SelectWrapper<W, T> selectWrapper, int pageNum, int pageSize);

}