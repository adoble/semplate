package templato.valuemap;

import java.util.*;

/**
 * Maps values to field names. 
 *  
 * @author Andrew Doble
 *
 */
public class ValueMap {
	
	Map<String, Object> valueMap = new HashMap<>();
	

	/**  
	 * Returns an optional value mapped to the specified field name. 
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
	 * If the fieldname has not been specified for the value map or the value is of type ValueMap then 
	 * an empty Optional is returned. 
	 * 
	 * @param fieldName The field name, either simple or compound. 
	 * @return An Optional containing the value. 
	 */
	public Optional<Object> getValue(String fieldName) {
		
		// Is the fieldname a compound
		int index = fieldName.indexOf('.');
		
		if (index == -1) { 
			// Not a compound
			Object value = valueMap.get(fieldName);
			if (value != null) {
				if (value instanceof ValueMap) return Optional.empty();
				else return Optional.of(value);
			} else return Optional.empty();
		} else {
			// Is a compound
	
			Optional<ValueMap> subValueMapOptional = this.getValueMap(fieldName.substring(0, index));
			Optional<Object> returnValue = subValueMapOptional.flatMap(svm -> svm.getValue(fieldName.substring(index +1)));
			return returnValue;
				
		}
	
	}
	
	
	/**  
	 * Returns an optional value map that is mapped to the specified field name. 
	 * 
	 * The field name  can be a simple field name, e.g.
	 *   <code>
	 *   assertEquals(referenceValueMap, (String) valueMap.getValue("reference");
	 *   </code>
	 *   
	 * or can be a compound field name such as:
	 *   <code>
	 *   assertEquals(referenceValueMap, (String) getValue("works.2.reference");
	 *   </code> 
	 * i.e. the value map of the second element* in "works" list. 
	 * 
	 * If the fieldname has not been specified for the value map or the value is <b>not of</b>  
	 * type ValueMap then an empty Optional is returned. 
	 * 
	 * @param fieldName The field name, either simple or compound. 
	 * @return An Optional containing the value map. 
	 */
	public Optional<ValueMap> getValueMap(String fieldName) {
		
		// Is the fieldname a compound
		int index = fieldName.indexOf('.');
		
		if (index == -1) { 
			// Not a compound
			Object value = valueMap.get(fieldName);
			if (value != null) {
				if (!(value instanceof ValueMap)) return Optional.empty();
				else return Optional.of((ValueMap) value);
			} else return Optional.empty();
		} else {
			// Is a compound
			Optional<ValueMap> subValueMapOptional = this.getValueMap(fieldName.substring(0, index));
			Optional<ValueMap> returnValue = subValueMapOptional.flatMap(svm -> svm.getValueMap(fieldName.substring(index +1)));
			return returnValue;
				
		}
		
	}

	/** 
	 * Returns a list of ValueMaps that are subordinate to the specified fieldname.
	 * For instance:
	 * <code>
	 *   ValueMap[] emperors = .....
	 *   ValueMap vm = new ValueMap();
	 *   vm.add("emperors", emperors[0]) 
	 *   vm.add("emperors", emperors[1]) 
	 *   vm.add("emperors", emperors[2])
	 *   emperors.put("emporers", vm);
	 *   emperors.getValueMaps("emperors");
	 * </code> 
	 * will return the value maps emperors[0], emperors[1] and emperors[2].
	 *    
	 * As such the method can be viewed as returning a list of the value maps that have been added to 
	 * a field. 
	 * 
	 * @param fieldName The field name of the value maps containing the ordinal field names
	 * @return A list of value maps 
	 */
	public List<ValueMap> getValueMaps(String fieldName) {
		List<ValueMap> list = new ArrayList<>();

		Optional<ValueMap> entry = this.getValueMap(fieldName);

		Set<String> fieldNameSet = entry.orElse(ValueMap.empty()).fieldNames();

		ValueMap ordinalEntry;
		for(String ordinalFieldName : fieldNameSet) {
			ordinalEntry = entry.orElse(ValueMap.empty());
			ordinalEntry.getValueMap(ordinalFieldName).ifPresent(vm -> list.add(vm));
		}

		return list;

	}
	
	/**
	 * Sets a mapping between the specified field name and a value object.
	 * 
	 * Field names can be simple (e.g. "name") or compound. Compound field names are formed 
	 * using a dot notation, e.g. 
	 *    emperor.name
	 * 
	 * Compound field names that reference ordinal lists of value maps are depicted so:
	 *    emperors.4.name
	 * i.e. the name of the 5th emperor added.
	 * 
	 *    
	 * @param fieldName The simple or compound field name
	 * @param dataObject  An object representing the value
	 * @return
	 */
	public Object put(String fieldName, Object dataObject) {
		
		int index = fieldName.indexOf('.');
		
		if (index == -1) {
			valueMap.put(fieldName, dataObject);
		} else {
			ValueMap subValueMap = new ValueMap();
			this.put(fieldName.substring(0, index), subValueMap);
			subValueMap.put(fieldName.substring(index + 1), dataObject);
		}
		
		return dataObject;
	}
	
		

	/**
	 * Add a value map to a field by automatically giving an ordinal number as field name.
	 * 
	 * For instance:
	 * <code>
	 *   ValueMap vm = new ValueMap();
	 *   vm.add("emperors", emperorValueMap[0]);
	 *   vm.add("emperors", emperorValueMap[1]);
	 * </code>  
	 * will:
	 * a) will create a new value map and map it to the field name "emperors"
	 * b) in the new value map will create a field "0" and map the value map emperorValueMap[0] to it. 
	 * c) in the new value map will create a field "1" and map the value map emperorValueMap[1] to it. 
	 * 
	 * Subsequent calls to add("emperors", ...) will create new mappings with the field name acting as 
	 * an incrementing ordinal number.
	 * 
	 * @param fieldName The name of field to contain the ordinal mappings to the value maps.
	 * @param map The value map to be added 
	 */
	public void add(String fieldName, ValueMap map) {
		ValueMap ordinalValueMap;
		
		if (!containsField(fieldName)) {
			ordinalValueMap = new ValueMap(); 
			put(fieldName, ordinalValueMap);
		}
		
		// Generate an ordinal number as key for the new value map, by first finding the key that 
		// represents the highest ordinal number and then creating a new key that represents a digit
		// one higher. 
		ordinalValueMap = getValueMap(fieldName).orElse(ValueMap.empty());
		Integer maxKey = ordinalValueMap.fieldNames().stream().mapToInt(Integer::parseInt).max().orElse(-1);
		ordinalValueMap.put(Integer.valueOf(maxKey + 1).toString(), map);
		
	}
	
	/**
	 * Merges a value map with this one. 
	 * @param other The value map to be merged with this. 
	 */
	public void merge(ValueMap other) {
		
		for (String fieldName: other.fieldNames()) {
			
			// Overwrite or add the entries that do not have a value map
			other.getValue(fieldName).ifPresent(obj -> this.put(fieldName, obj));
			
			// Add the ordinal value maps from the source to the target
			//other.getValueMap(fieldName).ifPresent(vm -> this.add(fieldName, vm));
			
			ValueMap ordinalValueMap = other.getValueMap(fieldName).orElse(ValueMap.empty());
			for (String ordinalFieldName : ordinalValueMap.fieldNames()) {
				ordinalValueMap.getValueMap(ordinalFieldName).ifPresent(vm -> this.add(fieldName, vm));
			}
		}
			
	}
	
	/**
	 * Creates a mutable empty value map.
	 * @return  A mutable empty value map.
	 */
	public static ValueMap empty() {
		return new ValueMap();
	}
	
	/**
	 * Generates a set with the field names on this value map 
	 * @return A set with the field names 
	 */
	public Set<String> fieldNames() {
		return valueMap.keySet();
	}
	

	/**
	 * Test if this value map is empty.
	 * @return True if this value map is empty.
	 */
	public boolean isEmpty() {
		return (valueMap.size() == 0);
	}
	
	/**
	 * Test if the specified field name maps to a value map.
	 * @return True if the specified field name maps to a value map.
	 */
	public boolean isValueMap(String fieldName) {
		return getValueMap(fieldName).isPresent();
	}

	/**
	 * Test if the specified field name maps has a mapping to an value.
	 * @return True iif the specified field name maps has a mapping to an value.
	 */
	public boolean containsField(String fieldName) {
		return (valueMap.containsKey(fieldName));
	}
	
	/**
	 * Returns a non-empty string representation of this ValueMaP suitable for debugging.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (Map.Entry<String, Object> entry: valueMap.entrySet()) {
				sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
		}

		return "(" + sb.toString().replaceAll(",$", "") + ")"; // Add parenthesis and remove any trailing commas

	}
	

}
