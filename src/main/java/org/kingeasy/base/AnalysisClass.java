package org.kingeasy.base;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * 解析类
 * 一个类对应一个解析类
 * 这个解析类，就是把类里的各个注解给解析出来并保存
 * 在通用的增删查改方法里面，就是到这个类里拿相应的信息进行操作
 */

public class AnalysisClass {

	/**
	 * 类所对应的表名
	 * 如果类添加了注解@TableName，则取注解的value作为表名
	 * 如果没有添加这个注解，则取类的简短名称并全转成小写，作为表名
	 */
	private String tableName;
	
	/**
	 * 类的主键字段名
	 * 也就是类添加了@PrimaryKey的成员变量的变量名
	 * 现在，暂时只支持一个类，只有一个主键的情况
	 */
	private String primaryKey;
	
	/**
	 * 类的主键对应表中的列名
	 * 也就是@PrimaryKey注解的value值
	 * 同时，@PrimaryKey注解也可以不添加value值，则默认为空字符串，那么就取字段的名称作为表的列名
	 */
	private String tablePrimaryKey;
	
	/**
	 * 类的字段名与表的列名的映射
	 * 如果字段名添加了@ClomnName注解，则会把注解的value作为值，把字段名作为键，存入fieldColumnMap中
	 * 如果字段名没有添加这个注解，则键和值都为字段名，存入fieldColumnMap中
	 * 同时，@PrimaryKey注解的映射，也会存入这个map中
	 */
	private Map<String, String> fieldColumnMap = new HashMap<String, String>();
	
	/**
	 * 表的列名与类的字段名的映射
	 * 与上一个map，只是键和值反过来存而已
	 */
	private Map<String, String> columnFieldMap = new HashMap<String, String>();
	
	/**
	 * 不能重复的字段映射
	 * 只有字段添加了@NoRepeat注解，才会把字段名作为键，字段的value作为值，存入这个map里面
	 */
	private Map<String, String> noRepeatMap = new HashMap<String, String>();
	
	/**
	 * 可以修改的字段集合
	 * 没有添加@Immtable注解的字段，才会加入到这个map里面
	 */
	private Set<String> modifiableField = new HashSet<String>();
	
	/**
	 * 限制删除的字段映射
	 * 添加了@NoDeletion注解的字段，字段名作键，注解的value作值，存入这个map
	 */
	private Map<String, String> noDeletionMap = new HashMap<String, String>();
	
	/**
	 * 本类查询条件映射
	 * 这个map只存放当前类里添加的@Search注解
	 * 以注解的param作为键，以字段对应的表的列名，加上前缀"t0."，再拼接注解的exp，作为值，存入这个map中
	 * 如果没有param的值，则默认为空字符串，那就以字段的名称作为键存入map中
	 */
	private Map<String, String> selfSearchExpMap = new HashMap<String, String>();
	
	/**
	 * 来自于其它类的查询条件映射
	 * 这个map用于存放所有来自于其它关联的类里的@Search注解的解析数据
	 * 存放的内容，与本类的查询条件唯一不同的值里的前缀，本类的都以"t0."作为前缀
	 * 而这里的前缀，以字段在类的定义里的顺序乘以10得到，比如，字段在类的定义里的顺序是在第6，那么前缀就是"t60."
	 * 在通用的查询方法里面，就是用t60作为关联类对应的表的别名
	 */
	private Map<String, String> otherSearchExpMap= new HashMap<String, String>();
	
	/**
	 * 统计注解映射
	 * 键是字段名，值是解析后的聚合函数表达式
	 */
	private Map<String, String> statisticsMap = new HashMap<String, String>();
	
	/**
	 * 字段与类的映射
	 * 当一个字段是关联的另一个类的时候，就会解析到这个map里面保存起来
	 * 键是字段名，值是另一个类的类文件
	 * 注：类文件是通过泛型来获取的，所以必须要使用泛型，才可以正确得到解析
	 * 比如字段定义为：private List<Item> items;就可以通过字段的定义，来获取Item类
	 * 如果写成：private List Items;那么是没法知道这个字段是关联哪个类的
	 */
	private Map<String, Class<?>> fieldAsClassMap = new HashMap<String, Class<?>>();
	
	/**
	 * 在@Mapping注解里，value是一个字符串数组，这个数据，用了一个类来解析，就是MapClass
	 * 这个map就是存放字段与MapClass的映射关系的
	 */
	private Map<String, MapClass> mapClassMap = new HashMap<String, MapClass>();
	
	/**
	 * 字段与字段的序号的映射
	 * 字段关联了另一个类，多表关联查询时，另一个表取别名，需要通过这个序号来指定
	 */
	private Map<String, Integer> fieldIndexMap = new HashMap<String, Integer>();
	
	/**
	 * 关联其它类，其它类的查询条件可能有多个，所以其它类的查询条件的键，要放到一个集合里面
	 * 以字段的序号为键，以集合为值，保存到这个map里面
	 */
	private Map<Integer, Set<String>> indexForOtherSearchList = new HashMap<Integer, Set<String>>();
	
	public AnalysisClass(){}
	
	public AnalysisClass(Class<?> clazz){
		analysisClass(clazz);
	}
	
	public void analysisClass(Class<?> clazz){
		setTableName(clazz);
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields){
			String fieldName = field.getName();
			setPrimaryKey(field, fieldName);
			putColumnAsField(field, fieldName);
			putStatistics(field, fieldName);
			putSelfSearch(field, fieldName);
			
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
	
	private void putSelfSearch(Field field, String fieldName){
		if(field.isAnnotationPresent(Searches.class)){
			Search[] searchs = field.getDeclaredAnnotationsByType(Search.class);
			for(int i=0,len=searchs.length; i<len; ++i){
				Search search = searchs[i];
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
					setManyToMany(field, index, mapping.value());
					break;
				default:
					System.out.println("异常数据！");
				}
			}
		}
	}
	
	private void setManyToMany(Field field, int index, String[] param){
		String fieldName = field.getName();
		MapClass innerClass = new MapClass(param[0], param[1], param[2]);
		mapClassMap.put(fieldName, innerClass);
		
		fieldAsClassMap.put(fieldName, KingUtils.getEntryType(field));
		
		Set<String> searches = new HashSet<String>();
		AnalysisClass otherAnalysisClass = Domain.getAnalysisClass(KingUtils.getEntryType(field));
		for(Map.Entry<String, String> entry : otherAnalysisClass.getSelfSearchExpMap().entrySet()){
			String value = new String(entry.getValue());
			value = value.replace("t0", "t"+(index+1));
			otherSearchExpMap.put(entry.getKey(), value);
			searches.add(entry.getKey());
		}
		
		indexForOtherSearchList.put(index, searches);
		fieldIndexMap.put(fieldName, index);
	}
	
	public Map<Integer, Set<String>> getIndexSearch(){
		return indexForOtherSearchList;
	}
	
	private void setTableName(Class<?> clazz){
		if(clazz.isAnnotationPresent(TableName.class)){
			tableName = clazz.getDeclaredAnnotation(TableName.class).value();
		}else{
			tableName = clazz.getSimpleName().toLowerCase();
		}
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void putStatistics(Field field, String fieldName){
		if(field.isAnnotationPresent(Statistic.class)){
			Statistic s = field.getDeclaredAnnotation(Statistic.class);
			String key = s.key();
			boolean handleNull = s.handleNull();
			switch(s.statistic()){
			case COUNT:
				statisticsMap.put(key, handleNull ? "COUNT(IFNULL(" + fieldName + ",0))" : "COUNT(" + fieldName + ")");
				break;
			case SUM:
				statisticsMap.put(key, handleNull ? "SUM(IFNULL(" + fieldName + ",0))" : "SUM(=" + fieldName + ")");
				break;
			case AVG:
				statisticsMap.put(key, handleNull ? "AVG(IFNULL(" + fieldName + ",0))" : "AVG(=" + fieldName + ")");
				break;
			case MAX:
				statisticsMap.put(key, handleNull ? "MAX(IFNULL(" + fieldName + ",0))" : "MAX(=" + fieldName + ")");
				break;
			case MIN:
				statisticsMap.put(key, handleNull ? "MIN(IFNULL(" + fieldName + ",0))" : "MIN(=" + fieldName + ")");
				break;
			default:
				System.out.println("异常数据！");
			}
		}
	}
	
	public void setPrimaryKey(Field field, String fieldName){
		if(field.isAnnotationPresent(PrimaryKey.class)){
			primaryKey = fieldName;
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
	
	public void putColumnAsField(Field field, String fieldName){
		if(field.isAnnotationPresent(ColumnName.class)){
			String columnName = field.getDeclaredAnnotation(ColumnName.class).value();
			fieldColumnMap.put(fieldName, columnName);
			columnFieldMap.put(columnName, fieldName);
		}else if(!field.isAnnotationPresent(Mapping.class)){
			fieldColumnMap.put(fieldName, fieldName);
			columnFieldMap.put(fieldName, fieldName);
		}
	}
	
	public Map<String, String> getFieldColumnMap() {
		return fieldColumnMap;
	}
	
	public Map<String, String> getNoRepeatMap() {
		return noRepeatMap;
	}
	
	public Set<String> getModifiableFieldSet() {
		return modifiableField;
	}
	
	public Map<String, String> getNoDeletionMap(){
		return noDeletionMap;
	}
	
	public Map<String, String> getSelfSearchExpMap(){
		return selfSearchExpMap;
	}
	
	public Map<String, String> getColumnFieldMap() {
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
	
	public Map<String, MapClass> getMapClassMap(){
		return mapClassMap;
	}
	
	public String getSelfField(String fieldName){
		return mapClassMap.get(fieldName).selfField();
	}
	
	public String getOtherField(String fieldName){
		return mapClassMap.get(fieldName).otherField();
	}
	
	public Map<String, String> getStatisticsMap(){
		return statisticsMap;
	}
	
	public Class<?> getMapClass(String key){
		return fieldAsClassMap.get(key);
	}
	
	public int getTableIndex(String key){
		return fieldIndexMap.get(key);
	}
}
