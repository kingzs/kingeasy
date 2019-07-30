package org.kingeasy.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilterTest {
	public static ObjectMapper mapper = new ObjectMapper();
	public static boolean kind = false;

	public static void main(String[] args) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		Domain.setRoadAsClass(new AnnotationScanner("king.test.entry").getRoadAsClassMap());
		Map<String, String> methodMap = new HashMap<String, String>();
		methodMap.put("save", "save");
		methodMap.put("search", "search");
		Domain.setMethodMap(methodMap);
		
		map.put("{\"name\":\"商品124\",\"image_addr\":\"image1.jpg\",\"price\":3.43,\"stock\":334}", null);
		String uri = "/product/save";
		filter(uri, map);
		map.clear();
		
		map.put("minStock", new String[]{"10"});
		uri = "/product/search";
		filter(uri, map);
	}
	
	public static void filter(String uri, Map<String, String[]> map){
		Service service = Domain.getService();
		if((uri.length() - uri.replace("/", "").length()) > 1){
			int index = uri.lastIndexOf("/");
			String methodKey = uri.substring(index+1);
			String entryKey = uri.substring(uri.lastIndexOf("/", index-1)+1, index);
			String methodName = Domain.getMethodName(methodKey);
			Class<?> clazz = Domain.getRoadClass(entryKey);
			
			if(methodName == null || clazz == null){
				System.out.println("路径不符，放行");
			}else{
				Object obj = null;
				Method method = getMethod(methodName);
				
				Map<String, Object> paramMap = new HashMap<String, Object>();

				for(Map.Entry<String, String[]> entry : map.entrySet()){
					String key = entry.getKey().trim();
					if(key.matches("^\\{.*\\}$")){
						try {
							obj = mapper.readValue(key, clazz);
						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else if(key.matches("^\\[.*\\]$")){
						paramMap.put("entrys", KingUtils.toList(new StringBuffer(key)));
					}else{
						paramMap.put(key, entry.getValue());
					}
				}
				
				
				try {
					if(kind){
						System.out.println(mapper.writeValueAsString(method.invoke(service, paramMap, clazz)));
					}else{
						System.out.println(mapper.writeValueAsString(method.invoke(service, obj)));
					}
				} catch(JsonProcessingException e1){
					e1.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if(t instanceof KingException){
						System.out.println(t.getMessage());
					}
				}
			}
		}else{
			System.out.println("没有对应的类或方法，放行");;
		}
	}
	
	public static Method getMethod(String methodName){
		if(methodName.equals("save") || methodName.equals("update") || methodName.equals("delete")){
			try {
				return Service.class.getMethod(methodName, Object.class);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}else{
			kind = true;
			try {
				return Service.class.getMethod(methodName, Map.class, Class.class);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void execute(){
		
	}
	
}
