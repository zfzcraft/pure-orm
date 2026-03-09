package cn.zfz.pureorm.core;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.enums.FieldKind;

public class TypeHandlerHelper {

	// ======================= 你要的唯一入口方法 =======================
	public static Object getRealValue(Class<?> entityClass, String columnName, Object value) {
		if (value == null) {
			return null;
		}

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta fieldMeta = entityMeta.getColumnNameMap().get(columnName);
		if (fieldMeta!=null) {
			if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
				return fieldMeta.getEnumTypeHandler().toDatabase((Enum<?>) value);
			}
		}
		
		return value;

	}

}
