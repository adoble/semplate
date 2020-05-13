package templato;

import java.util.*;

public class ValueMap {
	Map<String, Object> valueMap = new HashMap<String, Object>();

	public Object getDataValue(String fieldName) {
		return valueMap.get(fieldName);
	}
	
	@SuppressWarnings("unchecked")
	public List<ValueMap> getListValue(String fieldName) {
	  return (List<ValueMap>) getDataValue(fieldName);
		
	}

	public Object putObjectValue(String fieldName, Object dataObject) {
			valueMap.put(fieldName,  dataObject);
		
		return dataObject;
	}
	
	public List<ValueMap> putListValue(String fieldName, List<ValueMap> list) {
			valueMap.put(fieldName, list);
		
		return list; 
	}
	
	public void putAll(ValueMap map) {
		valueMap.putAll(map.valueMap);
	}

	public boolean isList(String fieldName) {
		return (containsField(fieldName) && (valueMap.get(fieldName) instanceof List)) ;
	}

	public boolean containsField(String fieldName) {
		return (valueMap.containsKey(fieldName));
	}
}
;