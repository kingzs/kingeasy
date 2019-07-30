package org.kingeasy.base;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.kingeasy.annotation.RoadMapping;

public class AnnotationScanner {
	
	private Map<String, Class<?>> roadAsClassMap = new HashMap<>();
	
	public AnnotationScanner(){}
	
	public AnnotationScanner(String packageNames){
		scan(packageNames);
	}
	
	public void scan(String packageNames){
		if(packageNames == null){
			return;
		}
		packageNames = packageNames.trim();
		if("".equals(packageNames)){
			return;
		}
		String[] packageNameArray = packageNames.split("\\s*,\\s*");
		
		try {
			String projectPath = new File("").getCanonicalPath();
			for(String packageName : packageNameArray){
				findClass(projectPath, packageName);	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void findClass(String path, String packageName){
		String dirRoad = packageName.replace(".", File.separator);
		String jarRoad = packageName.replace(".", "/");
		File file = new File(path);
		File[] files = file.listFiles();
		for(File tempFile : files){
			if(tempFile.isDirectory()){
				String tempFilePath = tempFile.getAbsolutePath();
				if(tempFilePath.endsWith(dirRoad)){
					findClassByDir(tempFile, packageName);
				}else{
					findClass(tempFilePath, packageName);
				}
			}else if(tempFile.getName().endsWith(".jar")){
				findClassByJar(tempFile, jarRoad);
			}
		}
	}
	
	private void findClassByJar(File file, String packageName){
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file.getAbsolutePath());
			Enumeration<JarEntry> jars = jarFile.entries();
			while(jars.hasMoreElements()){
				JarEntry jarEntry = jars.nextElement();
				String jarEntryName = jarEntry.getName();
				if(jarEntryName.startsWith(packageName) && jarEntryName.endsWith(".class")){
					putRoadAsClassMap(jarEntryName.replace(".class", "").replace("/", "."));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(jarFile != null){
					jarFile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void findClassByDir(File file, String packageName){
		File[] files = file.listFiles();
		for(File tempFile:files){
			if(tempFile.isDirectory()){
				findClassByDir(tempFile, packageName + "." + tempFile.getName());
			}else if(tempFile.getName().endsWith(".class")){
				String classAllName = packageName + "." + tempFile.getName().replace(".class", "");
				putRoadAsClassMap(classAllName);
			}
		}
	}
	
	private void putRoadAsClassMap(String classAllName){
		try {
			Class<?> clazz = Class.forName(classAllName);
			String road = null;
			if(clazz.isAnnotationPresent(RoadMapping.class)){
				road = clazz.getDeclaredAnnotation(RoadMapping.class).value();
			}else{
				road = clazz.getSimpleName().toLowerCase();
			}
			
			roadAsClassMap.put(road, clazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, Class<?>> getRoadAsClassMap(){
		return roadAsClassMap;
	}
	
}
