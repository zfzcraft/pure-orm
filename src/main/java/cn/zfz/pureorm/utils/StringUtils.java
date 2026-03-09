package cn.zfz.pureorm.utils;

public class StringUtils {

	public static boolean isNotEmpty(String name) {
		if (null==name) {
			return false;
		}
		if (name.equals("")) {
			return false;
		}
		return true;
	}

	public static String camelToSnake(String str) {
	    if (str == null || str.isEmpty()) {
	        return str;
	    }
	    int len = str.length();
	    StringBuilder sb = new StringBuilder(len + 4); // 预分配，更快
	    sb.append(Character.toLowerCase(str.charAt(0)));

	    for (int i = 1; i < len; i++) {
	        char c = str.charAt(i);
	        if (Character.isUpperCase(c)) {
	            sb.append('_');
	            sb.append(Character.toLowerCase(c));
	        } else {
	            sb.append(c);
	        }
	    }
	    return sb.toString();
	}

	public static boolean isEmpty(String tableName) {
		return !isNotEmpty(tableName);
	}

	

}
