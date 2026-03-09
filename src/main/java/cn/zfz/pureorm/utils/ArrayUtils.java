package cn.zfz.pureorm.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

	public static List<Integer> toList(int[] affectedRowsArray) {
		ArrayList<Integer> list = new ArrayList<>();
		for (int affectedRow : affectedRowsArray) {
			list.add(affectedRow);
		}
		return list;
	}

}
