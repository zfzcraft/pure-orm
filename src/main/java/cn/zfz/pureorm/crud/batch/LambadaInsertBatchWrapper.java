package cn.zfz.pureorm.crud.batch;

import java.util.*;
import java.util.stream.Collectors;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;
import cn.zfz.pureorm.utils.CollectionUtils;

public class LambadaInsertBatchWrapper<E>  implements InsertBatchWrapper<LambadaInsertBatchWrapper<E>, E>{

	private List<List<Object>> columnValuesList;

	private String tableName;

	private List<String> columnNames;

	public LambadaInsertBatchWrapper() {
	}

	public void insertEntityList(List<E> entityList) {
		// 非空校验 - 实体不能为空
		if (CollectionUtils.isEmpty(entityList)) {
			throw new IllegalArgumentException("待解析实体对象不能为空");
		}
		Class<?> entityClass = entityList.get(0).getClass();
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
		this.columnNames = getColumnNames(entityMeta);
		for (E entity : entityList) {
			List<Object> columnValues = getColumnValues(entityMeta, entity);
			columnValuesList.add(columnValues);
		}
	}

	private List<String> getColumnNames(EntityMeta entityMeta) {
		List<FieldMeta> fieldList = entityMeta.getFieldList();
		return fieldList.stream().filter(ele -> ele.getFieldKind() != FieldKind.PRIMARY_KEY_AUTO_INCREMENT)
				.map(ele -> ele.getColumnName()).collect(Collectors.toList());

	}

	protected List<Object> getColumnValues(EntityMeta entityMeta, Object entity) {
		List<Object> valueList = new ArrayList<>();
		List<FieldMeta> fieldMetas = entityMeta.getFieldList();
		for (FieldMeta fieldMeta : fieldMetas) {
			if (fieldMeta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				continue;
			}else if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
				EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
				try {
					Enum<?> enumObject = (Enum<?>) fieldMeta.getField().get(entity);
					Object enumValue = enumTypeHandler.toDatabase(enumObject);
					valueList.add(enumValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new PureOrmException(e);
				}
			} else {
				try {
					Object columnValue = fieldMeta.getField().get(entity);
					valueList.add(columnValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new PureOrmException(e);
				}
			}
		}
		return valueList;
	}
	@Override
	public String getTableName() {
		return tableName;
	}
	@Override
	public List<String> getColumnNames() {
		return Collections.unmodifiableList(columnNames);
	}

	@Override
	public List<List<Object>> getColumnValuesList() {
		return Collections.unmodifiableList(columnValuesList);
	}

	public static <T> LambadaInsertBatchWrapper<T> of(List<T> entityList) {
		LambadaInsertBatchWrapper<T> insertWrapper = new LambadaInsertBatchWrapper<>();
		insertWrapper.insertEntityList(entityList);
		return insertWrapper;
	}

}