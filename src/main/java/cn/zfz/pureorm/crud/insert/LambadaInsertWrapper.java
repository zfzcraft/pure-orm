package cn.zfz.pureorm.crud.insert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaInsertWrapper<E> implements InsertWrapper<LambadaInsertWrapper<E>, E> {

	private String tableName;

	private List<String> columnNames = new ArrayList<>();

	private List<Object> columnValues = new ArrayList<>();

	private LambadaInsertWrapper() {
	}

	private LambadaInsertWrapper(E entity, boolean skipNullFields) {
		if (skipNullFields) {
			Class<?> entityClass = entity.getClass();
			EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
			this.tableName = entityMeta.getTableName();
			List<String> columnNames = getColumnNames(entityMeta);
			List<Object> columnValues = getColumnValues(entityMeta, entity);
			List<Integer> indexesToRemove = IntStream.range(0, columnValues.size()).filter(i -> {
				Object obj = columnValues.get(i);
				return obj == null;
			}).boxed().collect(Collectors.toList());
			Collections.sort(indexesToRemove, Collections.reverseOrder());
			for (int i : indexesToRemove) {
				columnNames.remove(i);
				columnValues.remove(i);
			}
			this.columnNames = columnNames;
			this.columnValues = columnValues;
		} else {
			Class<?> entityClass = entity.getClass();
			EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
			this.tableName = entityMeta.getTableName();
			this.columnNames = getColumnNames(entityMeta);
			this.columnValues = getColumnValues(entityMeta, entity);
		}

	}

	private List<String> getColumnNames(EntityMeta entityMeta) {
		List<FieldMeta> fieldList = entityMeta.getFieldList();
		return fieldList.stream().filter(ele -> ele.getFieldKind() != FieldKind.PRIMARY_KEY_AUTO_INCREMENT)
				.map(ele -> ele.getColumnName()).collect(Collectors.toList());

	}

	private List<Object> getColumnValues(EntityMeta entityMeta, E entity) {
		List<Object> valueList = new ArrayList<>();
		List<FieldMeta> fieldMetas = entityMeta.getFieldList();
		for (FieldMeta fieldMeta : fieldMetas) {
			if (fieldMeta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				continue;
			} else if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
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
	public List<Object> getColumnValues() {
		return Collections.unmodifiableList(columnValues);

	}

	public static <E> LambadaInsertWrapper<E> of() {
		LambadaInsertWrapper<E> insertWrapper = new LambadaInsertWrapper<>();
		return insertWrapper;
	}

	public static <E> LambadaInsertWrapper<E> of(E entity) {
		LambadaInsertWrapper<E> insertWrapper = new LambadaInsertWrapper<>(entity, false);
		return insertWrapper;
	}

	@Override
	public LambadaInsertWrapper<E> insert(LambadaColumn<E> column, Object value) {
		if (StringUtils.isEmpty(tableName)) {
			String tableName = LambadaCache.getLambadaMeta(column).getTableName();
			this.tableName = tableName;
		}
		String columnName = LambadaCache.getLambadaMeta(column).getColumnName();
		columnNames.add(columnName);
		columnValues.add(value);
		return this;
	}

	public static <E> LambadaInsertWrapper<E> of(E entity, boolean skipNullFields) {
		LambadaInsertWrapper<E> insertWrapper = new LambadaInsertWrapper<>(entity, skipNullFields);
		return insertWrapper;
	}

}