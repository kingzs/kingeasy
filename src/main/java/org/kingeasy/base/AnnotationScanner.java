package org.kingeasy.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.kingeasy.annotation.RoadMapping;

public class AnnotationScanner {

	public List<String> classNames = new ArrayList<String>();
	
	public AnnotationScanner(){}
	
	public Map<String, Class<?>> scan(String packageName){
		Map<String, Class<?>> roadAsClass = new HashMap<String, Class<?>>();
		if(packageName == null) return roadAsClass;
		try {
			String projectPath = new File("").getCanonicalPath();
			findClass(projectPath, packageName);
			for(String className : classNames){
				Class<?> clazz = Class.forName(className);
				if(clazz.isAnnotationPresent(RoadMapping.class)){
					roadAsClass.put(clazz.getDeclaredAnnotation(RoadMapping.class).value(), clazz);
				}else{
					roadAsClass.put(clazz.getSimpleName().toLowerCase(), clazz);
				}
			}
		} catch (IOException e) {
			roadAsClass.clear();
		} catch (ClassNotFoundException e) {
			roadAsClass.clear();
		}
		classNames.clear();
		return roadAsClass;
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
					classNames.add(jarEntryName.replace(".class", "").replace("/", "."));
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
				classNames.add(packageName + "." + tempFile.getName().replace(".class", ""));
			}
		}
	}
	
}
