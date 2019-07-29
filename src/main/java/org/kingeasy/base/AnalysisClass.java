package org.kingeasy.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kingeasy.annotation.ColumnName;
import org.kingeasy.annotation.Immutable;
import org.kingeasy.annotation.Mapping;
import org.kingeasy.annotation.NoDeletion;
import org.kingeasy.annotation.NoRepeat;
import org.kingeasy.annotation.PrimaryKey;
import org.kingeasy.annotation.Search;
import org.kingeasy.annotation.Searches;
import org.kingeasy.annotation.Statistic;
import org.kingeasy.annotation.TableName;

public class AnalysisClass {

	private String tableName;
	private String primaryKey;
	private String tablePrimaryKey;
	private Map<String, String> fieldColumnMap = new HashMap<String, String>();
	private Map<String, String> columnFieldMap = new HashMap<String, String>();
	private Map<String, MapClass> mapClassMap = new HashMap<String, MapClass>();
	private Map<String, String> noRepeatMap = new HashMap<String, String>();
	private List<String> modifiableField = new ArrayList<String>();
	private Map<String, String> noDeletionMap = new HashMap<String, String>();
	private Map<String, String> selfSearchExpMap = new HashMap<String, String>();
	private Map<String, String> otherSearchExpMap= new HashMap<String, String>();
	private Map<String, String> statisticsMap = new HashMap<String, String>();
	private Map<String, Class<?>> fieldAsClassMap = new HashMap<String, Class<?>>();
	private Map<String, Integer> tableForIndexMap = new HashMap<String, Integer>();
	private Map<Integer, Set<String>> indexForOtherSearchList = new HashMap<Integer, Set<String>>();
	private Map<Integer, List<String>> tableSearchMap = new HashMap<Integer, List<String>>();
	
	public AnalysisClass(){}
	
	public AnalysisClass(Class<?> clazz){
		setTableName(clazz);
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields){
			String fieldName = field.getName();
			putStatistics(field);
			setPrimaryKey(field);
			putColumnAsField(field);
			setSelfSearch(field, fieldName);
			
			if(field.isAnnotationPresent(NoRepeat.class)){
				noRepeatMap.put(fieldName, field.getDeclaredAnnotation(NoRepeat.class).value());
			}
			if(!field.isAnnotationPresent(Immutable.class) && !field.isAnnotationPresent(PrimaryKey.class)){
				modifiableField.add(fieldName);
			}
			if(field.isAnnotationPresent(NoDeletion.class)){
				noDeletionMap.put(fieldName, field.getDeclaredAnnotation(NoDeletion.class).value());
			}
		}
	}
	
	public void setSelfSearch(Field field, String fieldName){
		if(field.isAnnotationPresent(Searches.class)){
			Search[] searchs = field.getDeclaredAnnotationsByType(Search.class);
			for(Search search : searchs){
				String param = search.param();
				if("".equals(param)){
					selfSearchExpMap.put(fieldName, "t0." + fieldColumnMap.get(fieldName) + " " + search.exp());
				}else{
					selfSearchExpMap.put(param, "t0." + fieldColumnMap.get(fieldName) + " " + search.exp());
				}
			}
		}
		if(field.isAnnotationPresent(Search.class)){
			Search search = field.getDeclaredAnnotation(Search.class);
			String param = search.param();
			if("".equals(param)){
				selfSearchExpMap.put(fieldName, "t0." + fieldColumnMap.get(fieldName) + " " + search.exp());
			}else{
				selfSearchExpMap.put(param, "t0." + fieldColumnMap.get(fieldName) + " " + search.exp());
			}
		}
	}
	
	public void setOtherSearch(Class<?> clazz){
		Field[] fields = clazz.getDeclaredFields();
		int index = 0;
		for(Field field : fields){
			index += 10;
			if(field.isAnnotationPresent(Mapping.class)){
				Mapping mapping = field.getDeclaredAnnotation(Mapping.class);
				switch(mapping.tableRelation()){
				case ONE_TO_ONE:
					
					break;
				case ONE_TO_MANY:
					
					break;
				case MANY_TO_ONE:
					
					break;
				case MANY_TO_MANY:
					String[] param = mapping.value();
					MapClass innerClass = new MapClass(param[0], param[1], param[2]);
					mapClassMap.put(field.getName(), innerClass);
					
					fieldAsClassMap.put(field.getName(), KingUtils.getEntryType(field));
					
					List<String> list = new ArrayList<String>();
					Set<String> searches = new HashSet<String>();
					AnalysisClass otherAnalysisClass = Domain.getAnalysisClass(KingUtils.getEntryType(field));
					for(Map.Entry<String, String> entry : otherAnalysisClass.getSelfSearchExpMap().entrySet()){
						String value = new String(entry.getValue());
						value = value.replace("t0", "t"+(index+1));
						otherSearchExpMap.put(entry.getKey(), value);
						list.add(value);
						searches.add(entry.getKey());
					}
					
					tableSearchMap.put(index, list);
					indexForOtherSearchList.put(index, searches);
					tableForIndexMap.put(field.getName(), index);
					break;
				default:
					System.out.println("�쳣���ݣ�");
				}
			}
		}
	}
	
	public Map<Integer, Set<String>> getIndexSearch(){
		return indexForOtherSearchList;
	}
	
	public Map<Integer, List<String>> getTableForSearch(){
		return tableSearchMap;
	}
	
	public void setTableName(Class<?> clazz){
		if(clazz.isAnnotationPresent(TableName.class)){
			tableName = clazz.getDeclaredAnnotation(TableName.class).value();
		}else{
			tableName = clazz.getSimpleName().toLowerCase();
		}
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void putStatistics(Field field){
		if(field.isAnnotationPresent(Statistic.class)){
			Statistic s = field.getDeclaredAnnotation(Statistic.class);
			String key = s.key();
			boolean handleNull = s.handleNull();
			switch(s.statistic()){
			case COUNT:
				statisticsMap.put(key, handleNull ? "COUNT(IFNULL(" + field.getName() + ",0))" : "COUNT(" + field.getName() + ")");
				break;
			case SUM:
				statisticsMap.put(key, handleNull ? "SUM(IFNULL(" + field.getName() + ",0))" : "SUM(=" + field.getName() + ")");
				break;
			case AVG:
				statisticsMap.put(key, handleNull ? "AVG(IFNULL(" + field.getName() + ",0))" : "AVG(=" + field.getName() + ")");
				break;
			case MAX:
				statisticsMap.put(key, handleNull ? "MAX(IFNULL(" + field.getName() + ",0))" : "MAX(=" + field.getName() + ")");
				break;
			case MIN:
				statisticsMap.put(key, handleNull ? "MIN(IFNULL(" + field.getName() + ",0))" : "MIN(=" + field.getName() + ")");
				break;
			default:
				System.out.println("�쳣���ݣ�");
			}
		}
	}
	
	public void setPrimaryKey(Field field){
		if(field.isAnnotationPresent(PrimaryKey.class)){
			primaryKey = field.getName();
			tablePrimaryKey = field.getDeclaredAnnotation(PrimaryKey.class).value();
			if("".equals(tablePrimaryKey)){
				tablePrimaryKey = primaryKey;
			}
			fieldColumnMap.put(primaryKey, tablePrimaryKey);
			columnFieldMap.put(tablePrimaryKey, primaryKey);
		}
	}
	
	public String getPrimaryKey() {
		return primaryKey;
	}
	
	public String getTablePrimaryKey(){
		return tablePrimaryKey;
	}
	
	public void putColumnAsField(Field field){
		String fieldName = field.getName();
		if(field.isAnnotationPresent(ColumnName.class)){
			String columnName = field.getDeclaredAnnotation(ColumnName.class).value();
			fieldColumnMap.put(fieldName, columnName);
			columnFieldMap.put(columnName, fieldName);
		}else if(!field.isAnnotationPresent(Mapping.class)){
			fieldColumnMap.put(fieldName, fieldName);
			columnFieldMap.put(fieldName, fieldName);
		}
	}
	
	public Map<String, String> getColumnAsFieldMap() {
		return fieldColumnMap;
	}
	
	public Map<String, String> getNoRepeatMap() {
		return noRepeatMap;
	}
	
	public List<String> getModifiableField() {
		return modifiableField;
	}
	
	public Map<String, String> getNoDeletionMap(){
		return noDeletionMap;
	}
	
	public Map<String, String> getSelfSearchExpMap(){
		return selfSearchExpMap;
	}
	
	public Map<String, String> getFieldAsColumnMap() {
		return columnFieldMap;
	}
	
	public String getExp(String param){
		String exp = selfSearchExpMap.get(param);
		return exp == null ? otherSearchExpMap.get(param) : exp;
	}
	public String getColumnName(String fieldName){
		return fieldColumnMap.get(fieldName);
	}
	public String getFieldName(String columnName){
		return columnFieldMap.get(columnName);
	}
	
	public String getMapTableName(String fieldName){
		return mapClassMap.get(fieldName).getTableName();
	}
	
	public Map<String, MapClass> getInnerClassMap(){
		return mapClassMap;
	}
	
	public String getSelfField(String fieldName){
		return mapClassMap.get(fieldName).selfField();
	}
	
	public String getOtherField(String fieldName){
		return mapClassMap.get(fieldName).otherField();
	}
	
	public Map<String, String> getStatisticsMap(){
		return this.statisticsMap;
	}
	
	public Class<?> getMapClass(String key){
		return fieldAsClassMap.get(key);
	}
	
	public int getTableIndex(String key){
		return tableForIndexMap.get(key);
	}
}
