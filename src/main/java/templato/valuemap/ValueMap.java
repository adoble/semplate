package templato.valuemap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps values to field names. 
 * 
 * 
 * 
 * @author Andrew Doble
 *
 */

public class ValueMap {
	
	Map<String, Object> valueMap = new HashMap<String, Object>();
	

	/**
	 * Returns the value mapped to the specified field name. 
	 * 
	 * The field name  can be a simple field name, e.g.
	 *   <code>
	 *   assertEquals("Plato", (String) valueMap.getValue("author");
	 *   </code>
	 *   
	 * or can be a compound field name such as:
	 *   <code>
	 *   assertEquals("Plato", (String) getValue("works.2.author");
	 *   </code> 
	 * i.e. the author of the second element* in "works" list. 
	 * * Strictly speaking this is the element that is mapped to 2 in the "works" list. 
	 *   
	 * @param fieldName The field name, either simple or compound. 
	 * @return An object representing the value. This can also be a ValueMap object.
	 */
	public Object getValue(String fieldName) {
		return valueMap.get(fieldName);
	}
	
	/**
	 * Returns a List of ValueMaps representing the fieldname/values. 
	 * 
	 * getList("works") 
	 * @param fieldName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ValueMap> getList(String fieldName) {
	  return (List<ValueMap>) getValue(fieldName);
		
	}
	
	public ValueMap getValueMap(String fieldName) {
		return (ValueMap) getValue(fieldName);
	}
	
	public boolean isValueMap(String fieldName) {
		return (getValue(fieldName) instanceof ValueMap);
	}
	
	public Object put(String fieldName, Object dataObject) {
		valueMap.put(fieldName,  dataObject);
		return dataObject;
	}
	
	public ValueMap add(String fieldName, ValueMap map) {
		if (containsField(fieldName) && isList(fieldName)) {
		   getList(fieldName).add(map);
		} else {
			// Create or overwrite
			List<ValueMap> listValueMaps = new ArrayList<>();
			listValueMaps.add(map);
			put(fieldName, listValueMaps);
		}
		
		return map; 
	}
	
	public void merge(ValueMap sourceMap) {
		Map<String, Object> mapWithoutLists = copyWithoutLists(sourceMap.valueMap);
		valueMap.putAll(mapWithoutLists);

		// Append the lists
		for (Map.Entry<String, Object> sourceEntry : sourceMap.valueMap.entrySet()) {
			if (sourceEntry.getValue() instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<ValueMap> sourceList = (List<ValueMap>) sourceMap.valueMap.getOrDefault(sourceEntry.getKey(), Collections.emptyList());
				@SuppressWarnings("unchecked")
				List<ValueMap> targetList = (List<ValueMap>) valueMap.get(sourceEntry.getKey());
				if (targetList != null) {
					targetList.addAll(sourceList);
				} else {
					valueMap.put(sourceEntry.getKey(), sourceList);
				}
			}
		}

	}

	public boolean isList(String fieldName) {
		return (containsField(fieldName) && (valueMap.get(fieldName) instanceof List)) ;
	}

	public boolean containsField(String fieldName) {
		return (valueMap.containsKey(fieldName));
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (Map.Entry<String, Object> entry: valueMap.entrySet()) {
			if (entry.getValue() instanceof List<?>) {
				List<ValueMap> listValueMaps = (List<ValueMap>) entry.getValue();
				sb.append(entry.getKey()).append('=');
				sb.append('[');
				for (ValueMap v: listValueMaps) {
					//sb.append(v.toString()).append(',').append('\n'); 
					sb.append(v.toString()).append('\n'); 
					sb.append(" ".repeat(entry.getKey().length() + 2));  // Indentation
				}
				sb.append(']').append('\n');
			} else {
				// Object value
				sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
			}

		}


		return "(" + sb.toString().replaceAll(",$", "") + ")"; // Add parenthesis and remove any trailing commas

	}
	
	private static Map<String, Object> copyWithoutLists(Map<String, Object> map) {
		return map.entrySet().stream().filter(e -> !(e.getValue() instanceof List<?>)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}


}
