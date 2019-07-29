package org.kingeasy.base;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Domain {

	private static Domain domain = new Domain();
	private final Map<Class<?>, AnalysisClass> classMap = new HashMap<Class<?>, AnalysisClass>();
	private Map<String, Class<?>> roadAsClass;
	private Map<String, String> methodMap;
	private Service service;
	private ObjectMapper mapper;
	
	private Domain(){
		service = new Service();
		mapper = new ObjectMapper();
	}
	
	public static Service getService(){
		return domain.service;
	}
	
	public static ObjectMapper getMapper(){
		return domain.mapper;
	}
	
	public static AnalysisClass getAnalysisClass(Class<?> clazz){
		if(clazz == null){
			return null;
		}
		
		AnalysisClass aClass = domain.classMap.get(clazz);
		if(aClass == null){
			synchronized(clazz){
				aClass = domain.classMap.get(clazz);
				if(aClass == null){
					aClass = new AnalysisClass(clazz);
					domain.classMap.put(clazz, aClass);
				}
				aClass.setOtherSearch(clazz);
			}
		}
		
		return aClass;
	}
	
	public static void setRoadAsClass(Map<String, Class<?>> roadAsClass){
		domain.roadAsClass = roadAsClass;
	}
	
	public static Class<?> getRoadClass(String entryKey){
		return domain.roadAsClass.get(entryKey);
	}
	
	public static String getMethodName(String methodKey){
		return domain.methodMap.get(methodKey);
	}
	
	public static void setMethodMap(Map<String, String> methodMap){
		domain.methodMap = methodMap;
	}
}
