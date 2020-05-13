package templato;

import java.util.*;

public class ValueMap {
	Map<String, Object> valueMap = new HashMap<String, Object>();

	public Object getDataObject(String fieldName) {
		return valueMap.get(fieldName);
	}
	
	@SuppressWarnings("unchecked")
	public List<ValueMap> getDataList(String fieldName) {
	  return (List<ValueMap>) getDataObject(fieldName);
		
	}

	public Object putObject(String fieldName, Object dataObject) {
			valueMap.put(fieldName,  dataObject);
		
		return dataObject;
	}
	
	public List<ValueMap> putList(String fieldName, List<ValueMap> list) {
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