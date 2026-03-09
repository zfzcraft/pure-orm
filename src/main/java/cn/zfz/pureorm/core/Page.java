package cn.zfz.pureorm.core;

import java.util.List;

public class Page<T> {

	private List<T> records;
	
	private int pageNum;
	private int pageSize;
	
	private Long total;

	public Page(List<T> records, int pageNum, int pageSize, Long total) {
		super();
		this.records = records;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.total = total;
	}

	public List<T> getRecords() {
		return records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public static <T> Page<T> of(List<T> records, int pageNum, int pageSize, long total) {
		return new Page<>(records, pageNum, pageSize, total);
	}
	
	
}
