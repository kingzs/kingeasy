package org.kingeasy.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditionAndValue {

	private String condition;
	private Set<String> conditionKeySet = new HashSet<String>();
	private List<Object> conditionList = new ArrayList<Object>();
	
	public ConditionAndValue(){}
	
	public ConditionAndValue(Map<String, String[]> paramMap, Class<?> clazz){
		AnalysisClass aClass = Domain.getAnalysisClass(clazz);
		StringBuffer buffer = new StringBuffer();
		paramMap.forEach((param, values) -> {
			if(values != null && values.length>0){
				conditionKeySet.add(param);
				if(values.length == 1){
					buffer.append(" AND " + aClass.getExp(param));
					conditionList.add(values[0]);
				}else{
					StringBuffer temp = new StringBuffer();
					for(int i=0, len=values.length; i<len; ++i){
						temp.append(" OR " + aClass.getExp(param));
						conditionList.add(values[i]);
					}
					buffer.append(" AND (" + temp.substring(4)+")");
				}
			}
		});
		if(buffer.length() > 0){
			condition = buffer.substring(5);
		}
	}
	
	public String getCondition(){
		return condition;
	}
	
	public List<Object> getConditionList(){
		return conditionList;
	}
	
	public Set<String> getConditionKeySet(){
		return conditionKeySet;
	}
}
