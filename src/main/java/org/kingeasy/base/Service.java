package org.kingeasy.base;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service {
	
	public static boolean flag = false;
	
	public int save(Object obj) throws KingException {
		AnalysisClass aClass = testRepeat(obj);
		
		StringBuffer columnNames = new StringBuffer(), placeholder = new StringBuffer();
		List<Object> fieldValueList = new ArrayList<Object>();
		aClass.getFieldColumnMap().forEach((fieldName, columnName) -> {
			Object fieldValue = KingUtils.getValue(obj, fieldName);
			if(!KingUtils.empty(fieldValue)){
				columnNames.append("," + columnName);
				fieldValueList.add(fieldValue);
				placeholder.append(",?");
			}
		});
		
		String sql = "INSERT INTO "+aClass.getTableName() + "(" + columnNames.substring(1)+") VALUES(" + placeholder.substring(1)+")";
		if(flag) System.out.println(sql);

		KData kData = new KData();
		int id = kData.executeUpdate(sql, fieldValueList);
		kData.close();
		return id;
	}
	
	public int update(Object obj) throws KingException {
		AnalysisClass aClass = testRepeat(obj);
		
		StringBuffer columnNames = new StringBuffer();
		List<Object> fieldValueList = new ArrayList<Object>();
		aClass.getModifiableFieldSet().forEach((fieldName) -> {
			Object fieldValue = KingUtils.getValue(obj, fieldName);
			columnNames.append(","+aClass.getColumnName(fieldName)+"=?");
			fieldValueList.add(fieldValue);
		});
		
		String sql = "UPDATE "+aClass.getTableName()+" SET "+columnNames.substring(1)+" WHERE "+aClass.getTablePrimaryKey()+"=?";
		if(flag) System.out.println(sql);
		fieldValueList.add(KingUtils.getValue(obj, aClass.getPrimaryKey()));
		
		KData kData = new KData(sql, fieldValueList);
		int num = kData.executeUpdate();
		kData.close();
		return num;	
	}
	
	public int delete(Object obj) throws KingException {
		AnalysisClass aClass = Domain.getAnalysisClass(obj.getClass());
		
		for(Map.Entry<String, String> entry : aClass.getNoDeletionMap().entrySet()){
			Object fieldValue = getValueForTable(obj, entry.getKey());
			if(fieldValue != null){
				String originalPrompt = entry.getValue();
				String relationFieldName = KingUtils.subBetween(originalPrompt, "{{", "}}");
				String info = KingUtils.getValue(fieldValue, relationFieldName).toString();
				throw new KingException(originalPrompt.replaceAll("\\{\\{.*\\}\\}", info));
			}
		}
		
		String sql = "DELETE FROM "+aClass.getTableName()+" WHERE "+aClass.getTablePrimaryKey()+"=?";
		if(flag) System.out.println(sql);
		
		List<Object> list = new ArrayList<Object>();
		list.add(KingUtils.getValue(obj, aClass.getPrimaryKey()));
		
		KData kData = new KData(sql, list);
		int num = kData.executeUpdate();
		kData.close();
		return num;
		
	}
	
	public <T> int delete(Object primaryKey, Class<T> clazz) throws KingException {
		try {
			T t = clazz.newInstance();
			KingUtils.copyProperty(t, Domain.getAnalysisClass(clazz).getPrimaryKey(), primaryKey);
			return delete(t);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public <T> List<T> search(Map<String, String[]> paramMap, Class<T> clazz) throws KingException {
		AnalysisClass aClass = Domain.getAnalysisClass(clazz);
		ConditionAndValue cav = new ConditionAndValue(paramMap, clazz);
		
		String tableFrom = aClass.getTableName() + " t0";
		for(Map.Entry<String, MapClass> entry : aClass.getMapClassMap().entrySet()){
			MapClass innerClass = entry.getValue();
			String fieldName = entry.getKey();
			int index = aClass.getTableIndex(fieldName);
			if(KingUtils.check(aClass.getIndexSearch().get(index), cav.getConditionKeySet())){
				tableFrom += " INNER JOIN " + innerClass.getTableName() + " t" + index + " ON t0." + aClass.getTablePrimaryKey() + "=t"+ index +"." + innerClass.selfField();
				AnalysisClass mapAnalysisClass = Domain.getAnalysisClass(aClass.getMapClass(entry.getKey()));
				++index;
				tableFrom += " INNER JOIN " + mapAnalysisClass.getTableName() + " t" + index + " ON t" + index + "." + mapAnalysisClass.getTablePrimaryKey() + "=t" + (index-1) + "." + innerClass.otherField();
			}
		}
		
		StringBuffer columnNameBuffer = new StringBuffer();
		aClass.getColumnFieldMap().forEach((columnName, fieldName) -> {
			columnNameBuffer.append(",t0."+columnName);
		});
		
		String sql = "SELECT " + columnNameBuffer.substring(1) + " FROM " + tableFrom;
		if(cav.getCondition() != null){
			sql += " WHERE " + cav.getCondition();
		}
		if(flag) System.out.println(sql);
		
		List<T> list = new ArrayList<T>();
		T t;
		
		KData kData = null;
		try {
			kData = new KData(sql, cav.getConditionList());
			kData.executeQuery();
			ResultSet rs = kData.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			while(rs.next()){
				t = clazz.newInstance();
				
				for(int i=0; i<colCount; ){
					String colName = rsmd.getColumnName(++i);
					Object value = rs.getObject(colName);
					KingUtils.copyProperty(t, aClass.getFieldName(colName), value);
				}
				list.add(t);
			}
			
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}catch(InstantiationException | IllegalAccessException e){
			throw new RuntimeException(e);
		}finally{
			if(kData != null){
				kData.close();
			}
		}
	}
	
	public <T> T search(Object primaryKeyValue, Class<T> clazz) throws KingException {
		AnalysisClass aClass = Domain.getAnalysisClass(clazz);
		
		StringBuffer columnNameBuffer = new StringBuffer();
		aClass.getColumnFieldMap().forEach((columnName, fieldName) -> {
			columnNameBuffer.append(","+columnName);
		});
		
		String sql = "SELECT " + columnNameBuffer.substring(1) + " FROM " + aClass.getTableName() + " WHERE " + aClass.getTablePrimaryKey() + "=?";
		
		T t = null;
		List<Object> list = new ArrayList<Object>();
		list.add(primaryKeyValue);
		
		KData kData = null;
		try {
			kData = new KData(sql, list);
			kData.executeQuery();
			ResultSet rs = kData.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			if(rs.next()){
				t = clazz.newInstance();
				for(int i=0; i<colCount; ){
					String colName = rsmd.getColumnName(++i);
					Object value = rs.getObject(colName);
					KingUtils.copyProperty(t, aClass.getFieldName(colName), value);
				}
			}
			return t;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}catch(InstantiationException | IllegalAccessException e){
			throw new RuntimeException(e);
		}finally{
			if(kData != null){
				kData.close();
			}
		}
	}
	
	public Map<String, Object> getStatistics(ConditionAndValue cav, Class<?> clazz){
		AnalysisClass aClass = Domain.getAnalysisClass(clazz);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if(aClass.getStatisticsMap().isEmpty()) return resultMap;
		StringBuffer columnBuffer = new StringBuffer();
		List<String> keyList = new ArrayList<String>();
		for(Map.Entry<String, String> entry : aClass.getStatisticsMap().entrySet()){
			columnBuffer.append("," + entry.getValue() + " " + entry.getKey());
			keyList.add(entry.getKey());
		}
		
		String sql = "SELECT " + columnBuffer.substring(1) + " FROM " + aClass.getTableName();
		if(cav.getCondition() != null){
			sql += " WHERE " + cav.getCondition();
		}
		if(flag) System.out.println(sql);
		
		KData kData = null;
		try {
			kData = new KData(sql, cav.getConditionList());
			kData.executeQuery();
			ResultSet rs = kData.getResultSet();
			if(rs.next()){
				for(String key : keyList){
					resultMap.put(key, rs.getObject(key));
				}
			}
			return resultMap;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			if(kData != null){
				kData.close();
			}
		}
	}
	
	public Map<String, Object> getStatistics(Map<String, String[]> paramMap, Class<?> clazz){
		return getStatistics(new ConditionAndValue(paramMap, clazz), clazz);
	}
	
	public long getCount(Map<String, String[]> paramMap, Class<?> clazz){
		AnalysisClass aClass = Domain.getAnalysisClass(clazz);
		ConditionAndValue cav = new ConditionAndValue(paramMap, clazz);
		
		String sql = "SELECT COUNT(" + aClass.getTablePrimaryKey() + ") count FROM " + aClass.getTableName();
		if(cav.getCondition() != null){
			sql += " WHERE " + cav.getCondition();
		}
		if(flag) System.out.println(sql);
		
		
		KData kData = null;
		try {
			kData = new KData(sql, cav.getConditionList());
			kData.executeQuery();
			ResultSet rs = kData.getResultSet();
			rs.next();
			return rs.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}finally{
			if(kData != null){
				kData.close();
			}
		}
	}
	
	public void getRelationEntry(Object obj){
		StackTraceElement[] mStack = Thread.currentThread().getStackTrace();
		String methodName = mStack[2].getMethodName();
		String fieldName = KingUtils.getFieldName(methodName);
		Class<?> clazz = null;
		List<Object> entries = new ArrayList<Object>();
		try {
			clazz = obj.getClass();
			AnalysisClass aClass = Domain.getAnalysisClass(clazz);
			Field field = clazz.getDeclaredField(fieldName);
			ParameterizedType t = (ParameterizedType) field.getGenericType();
			Type[] tts = t.getActualTypeArguments();
			Class<?> type = (Class<?>) tts[0];
			AnalysisClass oClass = Domain.getAnalysisClass(type);
			
			StringBuffer columnNameBuffer = new StringBuffer();
			oClass.getColumnFieldMap().forEach((cName, fName) -> {
				columnNameBuffer.append(",t0."+cName);
			});
			
			String sql = "SELECT " + columnNameBuffer.substring(1) + " FROM " + aClass.getTableName() + " t2" + " INNER JOIN " + aClass.getMapTableName(fieldName) + 
					" t1 ON t2." + aClass.getTablePrimaryKey() + "=t1." + aClass.getSelfField(fieldName) + " INNER JOIN " + oClass.getTableName() + " t0 ON t1." + 
					aClass.getOtherField(fieldName) + "=t0." +oClass.getTablePrimaryKey() + " WHERE t2." + aClass.getTablePrimaryKey() + "=?" ;
			
			List<Object> list = new ArrayList<Object>();
			list.add(KingUtils.getValue(obj, aClass.getPrimaryKey()));
			KData kData = new KData(sql, list);
			kData.executeQuery();
			
			ResultSet rs = kData.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			Object object = null;
			while(rs.next()){
				object = type.newInstance();
				
				for(int i=0; i<colCount; ){
					String colName = rsmd.getColumnName(++i);
					Object value = rs.getObject(colName);
					KingUtils.copyProperty(object, oClass.getFieldName(colName), value);
				}
				entries.add(object);
			}
			
			kData.close();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (KingException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		try {
			Method method = clazz.getMethod("s"+methodName.substring(1), List.class);
			method.invoke(obj, entries);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public AnalysisClass testRepeat(Object obj) throws KingException {
		AnalysisClass aClass = Domain.getAnalysisClass(obj.getClass());
		boolean b = true;
		Object primaryKeyValue = KingUtils.getValue(obj, aClass.getPrimaryKey());
		if(KingUtils.empty(primaryKeyValue)){
			b = false;
		}
		
		for(Map.Entry<String, String> entry : aClass.getNoRepeatMap().entrySet()){
			String fieldName = entry.getKey();
			Object value = KingUtils.getValue(obj, fieldName);
			if(value == null){
				continue;
			}
			
			String sql = "SELECT 1 FROM " + aClass.getTableName() + " WHERE " + aClass.getColumnName(fieldName) + "=?";
			
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			//如果主键不为空，代表是编辑，则判断条件要排除自身的重复，也就是不和自身判重
			if(b){
				sql += " AND " + aClass.getTablePrimaryKey() + "!=?";
				list.add(primaryKeyValue);
			}
			KData kData = null;
			try{
				kData = new KData(sql, list);
				kData.executeQuery();
				if(kData.getResultSet().next()){
					throw new KingException(entry.getValue());
				}
			}catch(SQLException e){
				e.printStackTrace();
			}finally{
				if(kData != null){
					kData.close();
				}
			}
		}
		return aClass;
	}

	public String getValueForTable(Object obj, String fieldName){
		
		return null;
	}
	
}
