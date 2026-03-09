package cn.zfz.pureorm.crud.upsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;

// Upsert = Insert + Update（存在则更新，不存在则插入）
public class LambadaUpsertWrapper<T> implements UpsertWrapper<LambadaUpsertWrapper<T>, T>{
    
    // 冲突判断的主键/唯一键（如 id、user_name）
    private String conflictKey;
    
    private String tableName;

	private List<String> insertColumnNames;

	private List<Object> insertColumnValues;
	
	private List<String> updateColumnNames;

	private List<Object> updateColumnValues;
	
	@Override
	public String getTableName() {
		return tableName;
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



	// 链式添加更新字段-值
    public LambadaUpsertWrapper<T> update(String column, Object value) {
        this.updateColumnNames.add(column);
        this.updateColumnValues.add(value);
        return this;
    }

    // 设置冲突键（主键/唯一键）
    public LambadaUpsertWrapper<T> conflictKey(String conflictKey) {
        this.conflictKey = conflictKey;
        return this;
    }

    

    public void setConflictKey(String conflictKey) {
		this.conflictKey = conflictKey;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	
	@Override
	public String getConflictKey() {
        return conflictKey;
    }

	public LambadaUpsertWrapper(T entity) {
		// 非空校验 - 实体不能为空
		if (Objects.isNull(entity)) {
			throw new IllegalArgumentException("待解析实体对象不能为空");
		}
		Class<?> entityClass = entity.getClass();
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
		this.insertColumnNames = getColumnNames(entityMeta);
		this.insertColumnValues = getColumnValues(entityMeta, entity);
		this.updateColumnNames = this.insertColumnNames;
		this.updateColumnValues = this.insertColumnValues;
		this.conflictKey = entityMeta.getPrimaryKeyField().getColumnName();
		
	}
	
	public LambadaUpsertWrapper(Class<T> entityClass) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
	}



	protected List<Object> getColumnValues(EntityMeta entityMeta, Object entity) {
		List<Object> valueList = new ArrayList<>();
		List<FieldMeta> fieldMetas = entityMeta.getFieldList();
		for (FieldMeta fieldMeta : fieldMetas) {
			if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
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
	
	private List<String> getColumnNames(EntityMeta entityMeta) {
		List<FieldMeta> fieldList = entityMeta.getFieldList();
		return fieldList.stream().map(ele -> ele.getColumnName()).collect(Collectors.toList());

	}

	public static <T> LambadaUpsertWrapper<T> of(T entity) {
		return new LambadaUpsertWrapper<>(entity);
	}



	@Override
	public LambadaUpsertWrapper<T> insert(LambadaColumn<T> column, Object value) {
		// TODO Auto-generated method stub
		return null;
	}


	public static <T> LambadaUpsertWrapper<T> of(T entity, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
}