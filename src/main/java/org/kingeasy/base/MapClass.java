package org.kingeasy.base;

public class MapClass {
	private String tableName;
	private String selfField;
	private String otherField;
	
	public MapClass(String tableName, String selfField, String otherField){
		this.tableName = tableName;
		this.selfField = selfField;
		this.otherField = otherField;
	}
	public String getTableName(){
		return tableName;
	}
	public String selfField(){
		return selfField;
	}
	public String otherField(){
		return otherField;
	}
}
