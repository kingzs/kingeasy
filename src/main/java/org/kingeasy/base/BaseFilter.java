package org.kingeasy.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseFilter implements Filter {
	private String encoding;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String dateFormat = filterConfig.getInitParameter("dateFormat");
		encoding = filterConfig.getInitParameter("encoding");
		String packageName = filterConfig.getInitParameter("packageName");
		
		if(dateFormat == null || "".equals(dateFormat)){
			Domain.getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		}else{
			Domain.getMapper().setDateFormat(new SimpleDateFormat(dateFormat));
		}
		if(encoding == null){
			encoding = "UTF-8";
		}
		if(packageName == null || "".equals(packageName)){
			Domain.setRoadAsClass(new HashMap<>());
		}else{
			Domain.setRoadAsClass(new AnnotationScanner().scan(packageName));
		}
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		
		final HttpServletRequest request = (HttpServletRequest) arg0;    
		HttpServletResponse response = (HttpServletResponse) arg1;
		request.setCharacterEncoding(encoding);
		response.setContentType("text/html; charset="+encoding);
		
		String uri = request.getRequestURI();
		if((uri.length() - uri.replace("/", "").length()) > 1){
			int index = uri.lastIndexOf("/");
			String methodKey = uri.substring(index+1);
			String entryKey = uri.substring(uri.lastIndexOf("/", index-1)+1, index);
			String methodName = Domain.getMethodName(methodKey);
			Class<?> clazz = Domain.getRoadClass(entryKey);
			if(methodName == null || clazz == null){
				arg2.doFilter(arg0, arg1);
			}else{
				boolean kind = false;
				Method method = null;
				Object obj = null;
				if(methodName.equals("save") || methodName.equals("update") || methodName.equals("delete")){
					try {
						method = Service.class.getMethod(methodName, Object.class);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}else{
					kind = true;
					try {
						method = Service.class.getMethod(methodName, Map.class, Class.class);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
				
				Map<String, Object> paramMap = new HashMap<String, Object>();

				for(Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()){
					String key = entry.getKey().trim();
					if(key.matches("^\\{.*\\}$")){
						obj = Domain.getMapper().readValue(key, clazz);
					}else if(key.matches("^\\[.*\\]$")){
						paramMap.put("entrys", KingUtils.toList(new StringBuffer(key)));
					}else{
						paramMap.put(key, entry.getValue());
					}
				}
				
				if(kind){
					try {
						method.invoke(null, paramMap, clazz);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}else{
					try {
						method.invoke(null, obj);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			arg2.doFilter(arg0, arg1);
		}
		
	}

	@Override
	public void destroy() {
		
	}

}