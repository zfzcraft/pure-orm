package cn.zfz.pureorm.crud.delete;

public class LambadaDeleteWrapper<E> extends DeleteWrapper<LambadaDeleteWrapper<E>, E> {

	public LambadaDeleteWrapper() {
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public static <T> LambadaDeleteWrapper<T> of() {
		return new LambadaDeleteWrapper<>();
	}
}
