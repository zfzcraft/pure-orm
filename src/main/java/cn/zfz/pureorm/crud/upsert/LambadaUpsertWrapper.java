package cn.zfz.pureorm.crud.upsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaUpsertWrapper<T> implements UpsertWrapper<LambadaUpsertWrapper<T>, T> {

	private String conflictKey;
	private String tableName;
	private final List<String> insertColumnNames = new ArrayList<>();
	private final List<Object> insertColumnValues = new ArrayList<>();
	private final List<String> updateColumnNames = new ArrayList<>();
	private final List<Object> updateColumnValues = new ArrayList<>();

	public LambadaUpsertWrapper() {
	}

	public LambadaUpsertWrapper(Class<T> entityClass) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
		if (entityMeta.getPrimaryKeyField() != null) {
			this.conflictKey = entityMeta.getPrimaryKeyField().getColumnName();
		}
	}

	public LambadaUpsertWrapper(T entity) {
		this(entity, false);
	}

	public LambadaUpsertWrapper(T entity, boolean skipNullFields) {
		if (Objects.isNull(entity)) {
			throw new IllegalArgumentException("待解析实体对象不能为空");
		}
		Class<?> entityClass = entity.getClass();
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
		FieldMeta pkField = entityMeta.getPrimaryKeyField();
		if (pkField != null) {
			this.conflictKey = pkField.getColumnName();
		}
		List<FieldMeta> fieldMetas = entityMeta.getFieldList();
		for (FieldMeta fieldMeta : fieldMetas) {
			try {
				Object value = fieldMeta.getField().get(entity);
				if (skipNullFields && value == null) {
					continue;
				}
				Object dbValue = value;
				if (fieldMeta.getFieldKind() == FieldKind.ENUM && value != null) {
					EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
					dbValue = enumTypeHandler.toDatabase((Enum<?>) value);
				}
				insertColumnNames.add(fieldMeta.getColumnName());
				insertColumnValues.add(dbValue);
				if (pkField == null || !fieldMeta.getColumnName().equals(pkField.getColumnName())) {
					updateColumnNames.add(fieldMeta.getColumnName());
					updateColumnValues.add(dbValue);
				}
			} catch (IllegalAccessException e) {
				throw new PureOrmException("获取字段值失败：" + fieldMeta.getField().getName(), e);
			}
		}
	}

	public LambadaUpsertWrapper<T> setConflictKey(LambadaColumn<T> column) {
		this.conflictKey = LambadaCache.getLambadaMeta(column).getColumnName();
		return this;
	}

	public LambadaUpsertWrapper<T> setConflictKey(String conflictKey) {
		this.conflictKey = conflictKey;
		return this;
	}

	public LambadaUpsertWrapper<T> setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	@Override
	public LambadaUpsertWrapper<T> insert(LambadaColumn<T> column, Object value) {
		if (StringUtils.isEmpty(tableName)) {
			tableName = LambadaCache.getLambadaMeta(column).getTableName();
		}
		String col = LambadaCache.getLambadaMeta(column).getColumnName();
		insertColumnNames.add(col);
		insertColumnValues.add(value);
		return this;
	}

	public LambadaUpsertWrapper<T> update(LambadaColumn<T> column, Object value) {
		if (StringUtils.isEmpty(tableName)) {
			tableName = LambadaCache.getLambadaMeta(column).getTableName();
		}
		String col = LambadaCache.getLambadaMeta(column).getColumnName();
		updateColumnNames.add(col);
		updateColumnValues.add(value);
		return this;
	}

	public LambadaUpsertWrapper<T> updateAllInsertColumns() {
		updateColumnNames.clear();
		updateColumnValues.clear();
		for (int i = 0; i < insertColumnNames.size(); i++) {
			if (!insertColumnNames.get(i).equals(conflictKey)) {
				updateColumnNames.add(insertColumnNames.get(i));
				updateColumnValues.add(insertColumnValues.get(i));
			}
		}
		return this;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getConflictKey() {
		return conflictKey;
	}

	@Override
	public List<String> getInsertColumnNames() {
		return insertColumnNames;
	}

	@Override
	public List<Object> getInsertColumnValues() {
		return insertColumnValues;
	}

	@Override
	public List<String> getUpdateColumnNames() {
		return updateColumnNames;
	}

	@Override
	public List<Object> getUpdateColumnValues() {
		return updateColumnValues;
	}

	public static <T> LambadaUpsertWrapper<T> of(T entity) {
		return new LambadaUpsertWrapper<>(entity);
	}

	public static <T> LambadaUpsertWrapper<T> of(T entity, boolean skipNullFields) {
		return new LambadaUpsertWrapper<>(entity, skipNullFields);
	}

	public static <T> LambadaUpsertWrapper<T> of(Class<T> entityClass) {
		return new LambadaUpsertWrapper<>(entityClass);
	}

}
