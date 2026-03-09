package cn.zfz.pureorm.utils;

import java.util.Collection;

public class CollectionUtils {

	public static boolean isEmpty(Collection<?> collections) {
		return collections==null || collections.isEmpty();
	}

}
