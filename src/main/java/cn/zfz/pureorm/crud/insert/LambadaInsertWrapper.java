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
	private final List<String> columnNames = new ArrayList<>();
	private final List<Object> columnValues = new ArrayList<>();

	private LambadaInsertWrapper(E entity, boolean skipNullFields) {
		Class<?> entityClass = entity.getClass();
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();

		List<String> names = getColumnNames(entityMeta);
		List<Object> values = getColumnValues(entityMeta, entity);

		if (skipNullFields) {
			List<Integer> indexesToRemove = IntStream.range(0, values.size())
					.filter(i -> values.get(i) == null)
					.boxed()
					.collect(Collectors.toList());
			Collections.sort(indexesToRemove, Collections.reverseOrder());
			for (int i : indexesToRemove) {
				names.remove(i);
				values.remove(i);
			}
		}

		this.columnNames.addAll(names);
		this.columnValues.addAll(values);
	}
	
	private LambadaInsertWrapper() {
		
	}

	private List<String> getColumnNames(EntityMeta entityMeta) {
		return entityMeta.getFieldList().stream()
				.filter(ele -> ele.getFieldKind() != FieldKind.PRIMARY_KEY_AUTO_INCREMENT)
				.map(FieldMeta::getColumnName)
				.collect(Collectors.toList());
	}

	private List<Object> getColumnValues(EntityMeta entityMeta, E entity) {
		List<Object> valueList = new ArrayList<>();
		for (FieldMeta fieldMeta : entityMeta.getFieldList()) {
			if (fieldMeta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				continue;
			}
			try {
				Object value = fieldMeta.getField().get(entity);
				if (fieldMeta.getFieldKind() == FieldKind.ENUM && value != null) {
					EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
					Enum<?> enumObject = (Enum<?>) value;
					valueList.add(enumTypeHandler.toDatabase(enumObject));
				} else {
					valueList.add(value);
				}
			} catch (IllegalAccessException e) {
				throw new PureOrmException(e);
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

	@Override
	public LambadaInsertWrapper<E> insert(LambadaColumn<E> column, Object value) {
		if (StringUtils.isEmpty(tableName)) {
			tableName = LambadaCache.getLambadaMeta(column).getTableName();
		}
		String columnName = LambadaCache.getLambadaMeta(column).getColumnName();
		columnNames.add(columnName);
		columnValues.add(value);
		return this;
	}

	public static <E> LambadaInsertWrapper<E> of(E entity, boolean skipNullFields) {
		return new LambadaInsertWrapper<>(entity, skipNullFields);
	}

	public static <E> LambadaInsertWrapper<E> of() {
		return new LambadaInsertWrapper<>();
	}

}