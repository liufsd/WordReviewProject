package com.coleman.http.json.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.coleman.http.json.annotation.KeyName;
import com.coleman.http.json.annotation.ValueObject;

public class TestBean {
	@KeyName(abbr = "A")
	private Integer age = Integer.MIN_VALUE;

	@KeyName(abbr = "D")
	private Date date = new Date(System.currentTimeMillis());

	@KeyName(abbr = "F")
	private int fee[] = new int[] { 1, 2, 3 };

	@KeyName(abbr = "I", genericType = Integer.class)
	private List<Integer> list = new ArrayList<Integer>();

	@KeyName(abbr = "ST")
	private String[] strs = new String[] { "aaa", "bbb" };

	@KeyName(abbr = "IB")
	private InnerBean[] beans = new InnerBean[] { new InnerBean(),
			new InnerBean() };

	@Override
	public String toString() {
		return "age:" + age + " date:" + date + " fee:" + fee[0] + "," + fee[1]
				+ "," + fee[2] + "...";
	}

	public TestBean() {
		list.add(1);
		list.add(2);
	}

	public int getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int[] getFee() {
		return fee;
	}

	public void setFee(int[] fee) {
		this.fee = fee;
	}

	public List<Integer> getList() {
		return list;
	}

	public void setList(List<Integer> list) {
		this.list = list;
	}

	public String[] getStrs() {
		return strs;
	}

	public void setStrs(String[] strs) {
		this.strs = strs;
	}

	public InnerBean[] getBeans() {
		return beans;
	}

	public void setBeans(InnerBean[] beans) {
		this.beans = beans;
	}

	@ValueObject
	public static class InnerBean {
		@KeyName(abbr = "NM")
		private String name = "inner";
		@KeyName(abbr = "AR")
		private int[] arrs = new int[] { 22, 33, 44 };

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int[] getArrs() {
			return arrs;
		}

		public void setArrs(int[] arrs) {
			this.arrs = arrs;
		}
	}
}
