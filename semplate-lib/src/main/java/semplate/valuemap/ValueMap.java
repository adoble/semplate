package semplate.valuemap;

import java.util.*;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import static com.google.common.base.Preconditions.*;

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
	 * 
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
	 * Graphically, with <i>VM</i> being a value map, <i>v</i> being a simple value and <i>f</i> a field name, an example 
	 * value map structure can be represented as:
	 * 
	 *          
	 *                        +-----------+
	 *                        |    VM0    |
	 *                        +--+-+-+-+--+
	 *        +----------------+ | | | | +---------------+
	 *        |                  | | | |                 |
	 *        |       +----------+ | | +---------+       |
	 *      f1|     f2|            | |         f5|     f6|
	 *        |       |        +---+ +--+        |       |
	 *        |       |      f3|      f4|        |       |
	 *      +---+  +--+--+  +-----+  +-----+  +--+--+  +---+
	 *      | v1|  | VM1 |  | VM2 |  | VM3 |  | VM4 |  | v2|
	 *      +---+  +-----+  ++-+-++  +-----+  +-----+  +---+
	 *                       | | |
	 *                +------+ | +-------+
	 *              f7|      f8|       f9|
	 *             +-----+  +-----+   +-----+
	 *             | VM5 |  | VM6 |   | VM7 |
	 *             +-----+  +-----+   +-----+
	 *
	 *
	 * Calling <code>VM0.getValueMaps(f3)</code> would return:
	 *       {VM5, VM6, VM7}
	 *       
	 * Calling <code>VM0.getValueMaps(f2)</code> would return:
	 *       {} , i.e an empty list
	 *      
	 * and calling <code>VM0.getValueMaps(f1)</code> would also return {}.
	 * 
	 * @see #getValueMaps()
	 *            
	 * @param fieldName The field name of the value maps containing the ordinal field names
	 * @return A list of value maps 
	 */
	public List<ValueMap> getValueMaps(String fieldName) {
//		List<ValueMap> list = new ArrayList<>();
//
//		Optional<ValueMap> entry = this.getValueMap(fieldName);
//
//		Set<String> fieldNameSet = entry.orElse(ValueMap.empty()).fieldNames();
//		
//
//		ValueMap ordinalEntry;
//		for(String ordinalFieldName : fieldNameSet) {
//			ordinalEntry = entry.orElse(ValueMap.empty());
//			ordinalEntry.getValueMap(ordinalFieldName).ifPresent(vm -> list.add(vm));
//		}
		
		checkArgument(this.isValueMap(fieldName), "Fieldname %s does not contain a value map", fieldName);
		
		ValueMap fieldVM = this.getValueMap(fieldName).orElse(ValueMap.empty());
		
		return fieldVM.getValueMaps();


	}
	
	/**  Returns a list of all value maps contained in the fields of this value map
	 * 
     * Graphically, with VM being a value map, v being a simple value and f a field name, an example 
	 * value map structure can be represented as:
	 * 
	 *          
	 *                        +-----------+
	 *                        |    VM0    |
	 *                        +--+-+-+-+--+
	 *        +----------------+ | | | | +---------------+
	 *        |                  | | | |                 |
	 *        |       +----------+ | | +---------+       |
	 *      f1|     f2|            | |         f5|     f6|
	 *        |       |        +---+ +--+        |       |
	 *        |       |      f3|      f4|        |       |
	 *      +---+  +--+--+  +-----+  +-----+  +--+--+  +---+
	 *      | v1|  | VM1 |  | VM2 |  | VM3 |  | VM4 |  | v2|
	 *      +---+  +-----+  ++-+-++  +-----+  +-----+  +---+
	 *                       | | |
	 *                +------+ | +-------+
	 *              f7|      f8|       f9|
	 *             +-----+  +-----+   +-----+
	 *             | VM5 |  | VM6 |   | VM7 |
	 *             +-----+  +-----+   +-----+
	 *
	 *
	 * Calling <code>VM0.getValueMaps()</code> would return:
	 *       {VM1, VM2, VM3, VM4}
	 *       
	 * Calling <code>VM1.getValueMaps()</code> would return:
	 *       {VM5, VM6, VM7}
	 *      
	 * and calling <code>VM4.getValueMaps</code> would return {}, i.e an empty list.
	 * 
	 * 
	 * @see #getValueMaps(String)
	 * @return A list of the value maps at the top level of this value map.
	 */
	public List<ValueMap> getValueMaps() {
		List<ValueMap> vmList = new ArrayList<>();
		
		Set<String> fieldNameSet = this.fieldNames();
		
		
		for (String fieldName: fieldNameSet) {
			if (this.isValueMap(fieldName)) {
				vmList.add(this.getValueMap(fieldName).get());
			}
		}
		
		return vmList;
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
	 * @return This value map.
	 * TODO check if the same return value is used for all like this. 
	 */
	public ValueMap put(String fieldName, Object dataObject) {
		
		int index = fieldName.indexOf('.');
		
		if (index == -1) {
			valueMap.put(fieldName, dataObject);
		} else {
			ValueMap subValueMap = new ValueMap();
			this.put(fieldName.substring(0, index), subValueMap);
			subValueMap.put(fieldName.substring(index + 1), dataObject);
		}
		
		//return dataObject;
		return this;
	}
	
	/**
	 * Add a value map to a list of value maps associated with a field		
	 * 
	 * For instance:
	 * <code>
	 *   ValueMap vm = new ValueMap();
	 *   vm.add("emperors", "0", emperorValueMap[0]);
	 *   vm.add("emperors", "1", emperorValueMap[1]);
	 * </code>  
	 * will:
	 * a) create a new value map and map it to the field name "emperors"
	 * b) in the new value map will create a field "0" and map the value map emperorValueMap[0] to it. 
	 * c) in the new value map will create a field "1" and map the value map emperorValueMap[1] to it. 
	 * 
	 * If the following line is then executed: 
	 * <code>
	 *   vm.add("emperors", "0", emperorValueMap[3]);
	 * </code>  
	 * then the field emperors.0 will be <strong>replaced</strong> by emperorValueMap[3]
	 * 
	 * Note: Although the parameter is called ordinalFieldName, the values do not need to be string 
	 * representations of integers.
	 * 
	 * @param fieldName The name of field to contain the mappings to the value maps
	 * @param ordinalFieldName The name of the field that the added value map is mapped to
	 * @param map The value map to be added 
	 * @return This value map
	 */
	public ValueMap add(String fieldName, String ordinalFieldName, ValueMap map) {
        ValueMap ordinalValueMap;
		
		ordinalValueMap = getValueMap(fieldName).orElse(ValueMap.empty());
		ordinalValueMap.put(ordinalFieldName, map);
		
	    this.put(fieldName, ordinalValueMap);
		
		return this;
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
	 * @param startingOrdinal The ordinal number to start from if no mappings exist
	 * @return This value map
	 */
	public ValueMap add(String fieldName, ValueMap map, int startingOrdinal) {
		ValueMap ordinalValueMap;
		
	
		// Generate an ordinal number as key for the new value map, by first finding the key that 
		// represents the highest ordinal number and then creating a new key that represents a digit
		// one higher. 
		ordinalValueMap = getValueMap(fieldName).orElse(ValueMap.empty());
		Integer maxKey = ordinalValueMap.fieldNames().stream().mapToInt(Integer::parseInt).max().orElse(startingOrdinal - 1);
		this.add(fieldName, Integer.valueOf(maxKey + 1).toString(), map);
				
		return this;
	}
	
	public ValueMap add(String fieldName, ValueMap map) {
		this.add(fieldName, map, 0); // Start a zero
		
		return this;
		
	}
	
	/**
	 * Merges a value map with this one. 
	 * @param other The value map to be merged with this. 
	 * @return This value map (containing the merge)
	 */
	public ValueMap merge(ValueMap other) {
		
		for (String fieldName: other.fieldNames()) {
			
			// Overwrite or add the entries that do not have a value map
			other.getValue(fieldName).ifPresent(obj -> this.put(fieldName, obj));
			
		    //other.getValueMap(fieldName).ifPresent(obj -> this.merge(obj));
		    
		    
		    if (other.isValueMap(fieldName)) {
		    	ValueMap otherFieldVM = other.getValueMap(fieldName).get();
		    	
		    	if (this.isValueMap((fieldName))) {
		    		ValueMap thisFieldVM = this.getValueMap(fieldName).get();
		    		thisFieldVM.merge(otherFieldVM);
		    	} else {
		    		//No field present
		    		this.put(fieldName, otherFieldVM);
		    	}
		    	
		    }
				
			
			
			// Add the ordinal value maps from the source to the target
			//other.getValueMap(fieldName).ifPresent(vm -> this.add(fieldName, vm));
			
//			ValueMap ordinalValueMap = other.getValueMap(fieldName).orElse(ValueMap.empty());
//			for (String ordinalFieldName : ordinalValueMap.fieldNames()) {
//				ordinalValueMap.getValueMap(ordinalFieldName).ifPresent(vm -> this.add(fieldName, vm));
//			}
		}
		
		return this;
			
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

   
	/** 
	 * Returns a value map containing one value. 
	 * 
	 * If the compound field name list contains a single element (e.g "nomen"), and the value is "Augustus", 
	 * then a value map is created with one field with one value, i.e. 
	 *      (nomen="Augustus")
     *
     * If the compound field name list contains a more then one element then a set of nested value maps 
     * are created, with the last field name in the list being the field name of the specified value,#
     * e.g. with a field name list of elements (emperors, 3, nomen) and the value of "Augustus", the 
     * following nested value ap structure is created: 
     * 
     *      (emperors=(3=(nomen="Augustus")))
	 * 
	 * @param compoundFieldName An ordered list of fieldnames. 
	 * @param value The value of the specified field
	 * @return A ValueMap containing the specified element
	 */
	public static ValueMap of(List<String> compoundFieldName, String value) {
		ValueMap valueMap = new ValueMap();
		
		// Now parse the fieldname
		List<String> fieldnameParts = compoundFieldName;
		
		if (fieldnameParts.size() == 1) { // This is a simple fieldname value pair
			valueMap.put(fieldnameParts.get(0), value);
		} else if  (fieldnameParts.size() > 1) {  
			// This is a multi-part fieldname so construct nested valueMaps and assign
			String fullFieldNameHead = fieldnameParts.get(0);
			List<String> fullFieldNameTail = fieldnameParts.subList(1, fieldnameParts.size()); 
			
			ValueMap vm = ValueMap.of(fullFieldNameTail, value);
			valueMap.put(fullFieldNameHead, vm);
			
		}
		
		return valueMap;
	}

	/** 
	 * Returns a value map containing one value constructed from the specified nameValuePair string. 
	 * 
	 * Examples: 
	 * If the name value pair parameter contains a single element (e.g "nomen"), and the value is "Augustus", then the name value pair 
	 * is specified using the following string:
	 *     "nomen=Augustus"
	 * This created a value map with one name value pair: 
	 *     (nomen="Augustus")
     *
     * If the name value pair parameter contains a list of fieldnames seperated with dot characters, then a set of nested value maps 
     * are created, with the last field name being the field name of the specified value, e.g. the string:
     * 
     *      "emperors.3.nomen=Augustus"
     *   
     * results in a nested value map being created, i.e. 
     * 
     *      (emperors=(3=(nomen="Augustus")))
	 * 
	 * @param nameValuePair A name value pair consisting of a dot separated list of bested field names, an equals sign and 
	 * then the value in quotes.
	 * @return A ValueMap containing the specified element
	 */ 
	public static ValueMap of(String nameValuePair) {
		// Extract the (possibly multi-part) field name and the value. 
		List<String> elements = Splitter.on('=')
				.trimResults(CharMatcher.is('\"')) //Quotes around values are removed
				.splitToList(nameValuePair);
		
				
		String fullFieldName = elements.get(0);
		String value = elements.get(1);
		
		// Now parse the fieldname
		List<String> fieldnameParts = Splitter.on('.').splitToList(fullFieldName);
		
		ValueMap vm = ValueMap.of(fieldnameParts, value);
		
		return vm;
	}


	 
	


}
