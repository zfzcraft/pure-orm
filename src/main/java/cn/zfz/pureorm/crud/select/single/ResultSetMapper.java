package cn.zfz.pureorm.crud.select.single;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;

public class ResultSetMapper {

	// 把 ResultSet 映射成 List<T>，扁平一对一
	@SuppressWarnings("unchecked")
	public static <T> List<T> map(ResultSet rs, Class<T> clazz) throws Exception {

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(clazz);
		List<T> list = new ArrayList<>();

		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();

		while (rs.next()) {
			T instance = clazz.getDeclaredConstructor().newInstance();

			for (int i = 1; i <= columnCount; i++) {
				// 数据库列名（比如 user_name）
				String columnName = meta.getColumnLabel(i);
				// 转成实体字段名 userName

				Object dbValue = rs.getObject(i);

				FieldMeta fieldMeta = entityMeta.getColumnNameMap().get(columnName);

				Field field = fieldMeta.getField();
				if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
					EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
					Object javaValue = enumTypeHandler.toJava((Class<? extends Enum<?>>) field.getType(), dbValue);
					field.set(instance, javaValue);
				} else {
					field.set(instance, dbValue);
				}
				// 扁平一对一赋值

			}
			list.add(instance);
		}
		return list;
	}

}
