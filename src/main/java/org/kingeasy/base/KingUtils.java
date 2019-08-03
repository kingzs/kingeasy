package org.kingeasy.base;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KingUtils {

	public static Class<?> getEntryType(Field field){
		ParameterizedType t = (ParameterizedType) field.getGenericType();
		Type[] types = t.getActualTypeArguments();
		return (Class<?>) types[0];
	}
	
	public static void sourceClose(Connection conn, PreparedStatement pstmt, ResultSet rs){
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(pstmt != null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Method getMethod(Class<?> clazz, String methodName){
		if(clazz == null || methodName == null || "".equals(methodName)){
			return null;
		}
		Method[] methods = clazz.getMethods();
		for(Method method : methods){
			if(method.getName().equals(methodName)){
				return method;
			}
		}
		return null;
	}
	
	public static void copyProperty(Object obj, String propertyName, Object value) throws KingException {
		try {
			Method method = getMethod(obj.getClass(), "set"+firstToUpper(propertyName));
			if(method == null){
				throw new KingException(propertyName + " 字段没有提供set方法，不能复制！");
			}
			Class<?> clazz = method.getParameterTypes()[0];
			
			if(clazz == int.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0);
				}else{
					method.invoke(obj, Integer.valueOf(value.toString()));
				}
			}else if(clazz == long.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0L);
				}else{
					method.invoke(obj, Long.valueOf(value.toString()));
				}
			}else if(clazz == double.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0.0);
				}else{
					method.invoke(obj, Double.valueOf(value.toString()));
				}
			}else if(clazz == String.class){
				if(value == null){
					method.invoke(obj, new Object[]{null});
				}else{
					method.invoke(obj, value.toString());
				}
			}else if(clazz == byte.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0);
				}else{
					method.invoke(obj, Byte.valueOf(value.toString()));
				}
			}else if(clazz == boolean.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, false);
				}else{
					method.invoke(obj, Boolean.valueOf(value.toString()));
				}
			}else if(clazz == char.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, ' ');
				}else{
					method.invoke(obj, Character.valueOf(value.toString().toCharArray()[0]));
				}
			}else if(clazz == float.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0.0f);
				}else{
					method.invoke(obj, Float.valueOf(value.toString()));
				}
			}else if(clazz == short.class){
				if(value == null || "".equals(value)){
					method.invoke(obj, 0);
				}else{					
					method.invoke(obj, Short.valueOf(value.toString()));
				}
			}else{
				if(value == null){
					method.invoke(obj, new Object[]{null});
				}else{
					method.invoke(obj, value);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> int getArrayLength(Object obj, Class<T> clazz){
		if(clazz == int.class){
			return ((int[]) obj).length;
		}else if(clazz == double.class){
			return ((double[]) obj).length;
		}else if(clazz == long.class){
			return ((long[]) obj).length;
		}else if(clazz == float.class){
			return ((float[]) obj).length;
		}else if(clazz == byte.class){
			return ((byte[]) obj).length;
		}else if(clazz == short.class){
			return ((short[]) obj).length;
		}else if(clazz == boolean.class){
			return ((boolean[]) obj).length;
		}else if(clazz == char.class){
			return ((char[]) obj).length;
		}else{
			return ((T[]) obj).length;
		}
	}
	
	public static boolean empty(Object obj){
		if(obj == null){
			return true;
		}
		//
		if(obj instanceof Collection){
			@SuppressWarnings("rawtypes")
			Collection coll = (Collection) obj;
			return coll.isEmpty();
		}
		//
		if(obj instanceof Map){
			@SuppressWarnings("rawtypes")
			Map map = (Map) obj;
			return map.isEmpty();
		}
		//�������������
		if(obj.getClass().isArray()){
			return getArrayLength(obj, obj.getClass().getComponentType())>0 ? false : true;
		}
		
		String str = obj.toString();
		//
		if("0".equals(str) || "0.0".equals(str) || str.matches("\\s*")){
			return true;
		}
		return false;
	}
	
	public static String firstToUpper(String str){
		return str.substring(0, 1).toUpperCase()+str.substring(1, str.length());
	}
	
	public static String subBetween(String original, String start, String end){
		int startIndex = original.indexOf(start)+start.length();
		int endIndex = original.indexOf(end);
		return original.substring(startIndex, endIndex);
	}
	
	public static Object getValue(Object obj, String fieldName){
		try {
			Method method = obj.getClass().getMethod("get"+firstToUpper(fieldName));
			return method.invoke(obj);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object toObject(String json){
		return toObject(new StringBuffer(json));
	}
	
	public static Object toObject(StringBuffer buffer){
		buffer = trim(buffer);
		if(buffer.length() == 0){
			return "";
		}
		if(buffer.charAt(0) == '{' && buffer.charAt(buffer.length()-1) == '}'){
			return toMap(buffer);
		}else if(buffer.charAt(0) == '[' && buffer.charAt(buffer.length()-1) == ']'){
			return toList(buffer);
		}else{
			return buffer.toString();
		}
	}
	
	public static Map<String, Object> toMap(StringBuffer buffer){
		buffer.delete(0, 1).delete(buffer.length()-1, buffer.length());
		Map<String, Object> map = new HashMap<String, Object>();
		while(buffer.length() > 0){
			int index = buffer.indexOf(":");
			if(index == -1){
				return map;
			}
			String key = trimQuotationMarks(buffer.substring(0, index).trim());
			trim(buffer.delete(0, ++index));
			if(buffer.charAt(0) == '{'){
				index = pair(buffer, '{', '}');
				map.put(key, toObject(buffer.substring(0, ++index)));
				trimLeftComma(buffer.delete(0, index));
			}else if(buffer.charAt(0) == '['){
				index = pair(buffer, '[', ']');
				map.put(key, toObject(buffer.substring(0, ++index)));
				trimLeftComma(buffer.delete(0, index));
			}else{
				index = buffer.indexOf(",");
				if(index == -1){
					map.put(key, trimQuotationMarks(buffer.substring(0, buffer.length()).trim()));
					buffer.delete(0, buffer.length());
				}else{
					map.put(key, trimQuotationMarks(buffer.substring(0, index).trim()));
					trim(buffer.delete(0, ++index));
				}
			}
		}
		return map;
	}
	
	public static List<Object> toList(StringBuffer buffer){
		buffer.delete(0, 1).delete(buffer.length()-1, buffer.length());
		List<Object> list = new ArrayList<Object>();
		while(buffer.length() > 0){
			if(buffer.charAt(0) == '{'){
				int index = pair(buffer, '{', '}');
				list.add(toObject(buffer.substring(0, ++index)));
				trimLeftComma(buffer.delete(0, index));
			}else if(buffer.charAt(0) == '['){
				int index = pair(buffer, '[', ']');
				list.add(toObject(buffer.substring(0, ++index)));
				trimLeftComma(buffer.delete(0, index));
			}else{
				int index = buffer.indexOf(",");
				if(index == -1){
					list.add(trimQuotationMarks(buffer.substring(0, buffer.length()).trim()));
					buffer.delete(0, buffer.length());
				}else{
					list.add(trimQuotationMarks(buffer.substring(0, index).trim()));
					trim(buffer.delete(0, ++index));
				}
			}
		}
		return list;
	}
	
	public static int pair(StringBuffer buffer, char left, char right){
		int count = 0;
		for(int i=0,len=buffer.length(); i<len; ++i){
			if(buffer.charAt(i) == left) ++count;
			if(buffer.charAt(i) == right){
				--count;
				if(count == 0) return i;
			}
		}
		return -1;
	}
	
	public static StringBuffer trimLeftComma(StringBuffer buffer){
		buffer = trim(buffer);
		if(buffer.length()>0 && buffer.charAt(0) == ','){
			return buffer.delete(0, 1);
		}
		return buffer;
	}
	
	public static String trimQuotationMarks(String value){
		if(value.startsWith("\"") && value.endsWith("\"")){
			return value.substring(1, value.length()-1);
		}
		return value;
	}
	
	public static StringBuffer trim(StringBuffer buffer){
		if(buffer == null){
			return new StringBuffer();
		}
		int st = 0;
		int len = buffer.length();
		while(st<len && buffer.charAt(st) <= ' '){
			++st;
		}
		if(st > 0) buffer.delete(0, st);
		len -= ++st;
		while(len>0 && buffer.charAt(len) <= ' '){
			--len;
		}
		if(len >= 0) buffer.delete(len+1, buffer.length());
		return buffer;
	}
	
	public static boolean check(Set<?> a, Set<?> b){
		Set<Object> temp = new HashSet<Object>(a);
		Iterator<?> it = b.iterator();
		while(it.hasNext()){
			if(!temp.add(it.next())){
				return true;
			}
		}
		return false;
	}
	
	public static String getFieldName(String methodName){
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}
	
	public static <T> List<T> arrayToList(T[] ts){
		List<T> list = new ArrayList<T>();
		if(ts == null || ts.length == 0){
			return list;
		}
		for(T t : ts){
			if(t != null){
				list.add(t);
			}
		}
		return list;
	}
	
}
