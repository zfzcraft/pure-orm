package cn.zfz.pureorm.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * JDBC标准类型校验工具（PureORM 内置白名单）
 */
public final class JdbcStandardTypeUtils {

   
    /**
     * 校验：是否为【String 模式 / 原生模式】允许的标准类型
     * 只允许：包装类、字符串、数字、现代时间、byte[]
     */
    public static boolean isSupported(Class<?> type) {
        if (type == null) return false;

        // 1. 数字包装类型（禁止基本类型）
        if (type == Boolean.class) return true;
        if (type == Byte.class) return true;
        if (type == Short.class) return true;
        if (type == Integer.class) return true;
        if (type == Long.class) return true;
        if (type == Float.class) return true;
        if (type == Double.class) return true;

        // 2. 字符串
        if (type == String.class) return true;

        // 3. 高精度数字
        if (type == BigDecimal.class) return true;

        // 4. 二进制
        if (type == byte[].class) return true;

        // 5. JDK8 现代时间（java.sql 全部丢弃）
        if (type == LocalDate.class) return true;
        if (type == LocalTime.class) return true;
        if (type == LocalDateTime.class) return true;
        if (type == OffsetDateTime.class) return true;

        // 都不是 → 不支持
        return false;
    }

    /**
     * 断言类型合法，不合法直接抛异常（你框架里直接用这个）
     */
    public static void assertSupported(Class<?> type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException(
                "PureORM 原生模式仅支持标准JDBC类型，不支持实体/枚举/自定义对象：" + type.getName()
            );
        }
    }
}
