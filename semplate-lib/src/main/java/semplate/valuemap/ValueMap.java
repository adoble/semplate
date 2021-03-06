package semplate.valuemap;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;

import static com.google.common.base.Preconditions.*;
import static com.google.common.primitives.Primitives.isWrapperType;

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
	 * <p>
	 *   <code>
	 *   assertEquals("Plato", (String) valueMap.getValue("author");
	 *   </code>
	 * <p> ...or can be a compound field name such as:
	 * <p>
	 *   <code>
	 *   assertEquals("Plato", (String) getValue("works.2.author");
	 *   </code> 
	 * <p>i.e. the author of the second element* in "works" list. 
	 *  Strictly speaking this is the element that is mapped to 2 in the "works" list. 
	 * <p>
	 * If the fieldname has not been specified for the value map or the value is of type ValueMap then 
	 * an empty Optional is returned. 
	 * 
	 * @param fieldName The field name, either simple or compound. 
	 * @return An Optional containing the value referenced by the field name or empty if the value could not be found
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
	 * <p>
	 * The field name  can be a simple field name, e.g.
	 * <p>
	 *   <code>
	 *   assertEquals(referenceValueMap, (String) valueMap.getValue("reference");
	 *   </code>
	 * <p>  
	 * ...or can be a compound field name such as:
	 * <p>
	 *   <code>
	 *   assertEquals(referenceValueMap, (String) getValue("works.2.reference");
	 *   </code> 
	 * <p>i.e. the value map of the second element* in "works" list. 
	 * <p>
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
	 * <p>
	 * For instance:
	 * 
	 * <blockquote>
	 * <pre>
	 *   ValueMap[] emperors = .....
	 *   ValueMap vm = new ValueMap();
	 *   vm.add("emperors", emperors[0]) 
	 *   vm.add("emperors", emperors[1]) 
	 *   vm.add("emperors", emperors[2])
	 *   emperors.put("emperors", vm);
	 *   emperors.getValueMaps("emperors");
	 * </pre>
	 * </blockquote> 
	 * 
	 * will return the value maps <code>emperors[0]</code>, <code>emperors[1]</code> and <code>emperors[2]</code>.
	 * <p>   
	 * As such the method can be viewed as returning a list of the value maps that have been added to 
	 * a field. 
	 * <p>
	 * Graphically, with <i>VM</i> being a value map, <i>v</i> being a simple value and <i>f</i> a field name, an example 
	 * value map structure can be represented as:
	 * 
	 * <pre>       
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
	 * </pre>
	 * 
	 * 
	 * <p>Calling <code>VM0.getValueMaps(f3)</code> would return:</p>
	 * <blockquote>      
	 *               {VM5, VM6, VM7} 
	 * </blockquote>
	 *       
	 * <p>Calling <code>VM0.getValueMaps(f2)</code> would return:</p>
	 * <blockquote>
	 *         { } , i.e an empty list
	 * </blockquote>
	 *      
	 * <p>and calling <code>VM0.getValueMaps(f1)</code> would also return { }.</p>
	 * 
	 * @see #getValueMaps()
	 *            
	 * @param fieldName The field name of the value maps containing the ordinal field names
	 * @return A list of value maps 
	 */
	public List<ValueMap> getValueMaps(String fieldName) {
		checkArgument(this.isValueMap(fieldName), "Fieldname %s does not contain a value map", fieldName);
		
		ValueMap fieldVM = this.getValueMap(fieldName).orElse(ValueMap.empty());
		
		return fieldVM.getValueMaps();


	}
	
	/**  Returns a list of all value maps contained in the fields of this value map
	 * <p>
     * Graphically, with VM being a value map, v being a simple value and f a field name, an example 
	 * value map structure can be represented as:
	 * 
	 * <pre>         
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
	 * </pre>
	 * 
	 * Calling <code>VM0.getValueMaps()</code> would return:
	 * <blockquote>
	 *       {VM1, VM2, VM3, VM4}
	 * </blockquote>
	 *       
	 * Calling <code>VM1.getValueMaps()</code> would return:
	 * <blockquote>
	 *       {VM5, VM6, VM7}
	 * </blockquote>     
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
	
	/** Sets a mapping between the specified field name and a value object.
	 * 
	 * 
	 * Field names can be simple (e.g. "name") or compound. Compound field names are formed 
	 * using a dot notation, e.g. 
	 *    emperor.name
	 * 
	 * A compound field name can contain any number of field names, e.g. 
	 *   emperor.5.consort.name
	 * 
	 * Numbers can be used for field names to represent an ordinal position  in a list. 
	 * In the above this refers to the 6th (5 + 1) emperor in a a list. As far as this method
	 * is concerned, this is only a convention. 
	 * 
	 * @param fieldName The simple or compound field name
	 * @param dataObject  An object representing the value
	 * @return This value map.
	 * TODO Do we need the add function at all? 
	 */
	public ValueMap put(String fieldName, Object dataObject) {
		
		int index = fieldName.indexOf('.');
		
		if (index == -1) {  // Simple field name so just replace the value 
			valueMap.put(fieldName, dataObject);
		} else {
			String fieldNameHead = fieldName.substring(0, index);
			String fieldNameTail = fieldName.substring(index + 1); // The leading dot is not included
			
			if (!this.containsField(fieldNameHead)) {
				valueMap.put(fieldNameHead, new ValueMap());
			}
			((ValueMap) valueMap.get(fieldNameHead)).put(fieldNameTail, dataObject);
		}
		
		return this;
	}
	
	/**  Add a value map to a list of value maps associated with a field.
	 * 	<p>	
	 * For instance:
	 * 
	 * <pre>
	 *   ValueMap vm = new ValueMap();
	 *   vm.add("emperors", "0", emperorValueMap[0]);
	 *   vm.add("emperors", "1", emperorValueMap[1]);
	 * </pre>
	 *   
	 * will:
	 * <ol>
	 * <li>create a new value map and map it to the field name "emperors"
	 * <li>in the new value map will create a field "0" and map the value map emperorValueMap[0] to it. 
	 * <li>in the new value map will create a field "1" and map the value map emperorValueMap[1] to it. 
	 * </ol>
	 * If the following line is then executed: 
	 * <p>
	 * <code>
	 *   vm.add("emperors", "0", emperorValueMap[3]);
	 * </code>  
	 * <p>
	 * then the field emperors.0 will be <strong>replaced</strong> by emperorValueMap[3]
	 * <p>
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


	
	
	
	/** Merges a value map with this one.
	 *  
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

		}

		return this;

	}
	
	/** Creates a mutable empty value map.
	 * 
	 * @return  A mutable empty value map.
	 */
	public static ValueMap empty() {
		return new ValueMap();
	}
	
	/** Generates a set with the field names on this value map 
	 * 
	 * @return A set with the field names 
	 */
	public Set<String> fieldNames() {
		return valueMap.keySet();
	}
	

	/** Test if this value map is empty.
	 * 
	 * @return True if this value map is empty.
	 */
	public boolean isEmpty() {
		return (valueMap.size() == 0);
	}
	
	/** Test if the specified field name maps to a value map.
	 * 
	 * @param fieldName The field name
	 * @return True if the specified field name maps to a value map.
	 */
	public boolean isValueMap(String fieldName) {
		return getValueMap(fieldName).isPresent();
	}

	/** Test if the specified field name maps has a mapping to an value.
	 * 
	 * @param fieldName The field name
	 * @return True if the specified field name maps has a mapping to an value.
	 */
	public boolean containsField(String fieldName) {
		int index = fieldName.indexOf('.');

		if (index == -1) {  // Simple field name so just replace the value 
			return (valueMap.containsKey(fieldName));
		} else {  // Compound field name
			String fieldNameHead = fieldName.substring(0, index);
			String fieldNameTail = fieldName.substring(index + 1); // The leading dot is not included
			ValueMap subValueMap = getValueMap(fieldNameHead).orElse(ValueMap.empty());
			if (!subValueMap.isEmpty()) {
				return subValueMap.containsField(fieldNameTail);
			} else {
				return false;
			}
		}    	
	}
	

	
	/**
	 * Returns a non-empty string representation of this ValueMaP suitable for debugging.
	 * 
	 * @return A string representation
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
	 * <p>
	 * If the compound field name list contains a single element (e.g "nomen"), and the value is "Augustus", 
	 * then a value map is created with one field with one value, i.e. 
	 * <pre>     (nomen="Augustus")  </pre>
     *
     * If the compound field name list contains a more then one element then a set of nested value maps 
     * are created, with the last field name in the list being the field name of the specified value,#
     * e.g. with a field name list of elements <code>(emperors, 3, nomen)</code> and the value of "Augustus", the 
     * following nested value ap structure is created: 
     * 
     * <pre>     (emperors=(3=(nomen="Augustus"))) </pre>
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
	 * <p>
	 * Examples: 
	 * <p>
	 * If the name value pair parameter contains a single element (e.g "nomen"), and the value is "Augustus", then the name value pair 
	 * is specified using the following string:
	 * <pre>
	 *     "nomen=Augustus"
	 * </pre>
	 * This created a value map with one name value pair: 
	 * <pre>
	 *     (nomen="Augustus")
	 * </pre>
     *
     * If the name value pair parameter contains a list of fieldnames seperated with dot characters, then a set of nested value maps 
     * are created, with the last field name being the field name of the specified value, e.g. the string:
     * <pre>
     *   "emperors.3.nomen=Augustus"
     * </pre>  
     * results in a nested value map being created, i.e. 
     * <pre>
     *      (emperors=(3=(nomen="Augustus")))
	 * </pre>
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

    /** Creates a value map from the specified data object.
     * 
     * @param dataObject An object whose fields are annotated with @{@link TemplateField}
     * @throws ConversionException if the data object cannot be converted to a value map.
     * @return The created value map
     */
    @SuppressWarnings("unchecked")
	public static ValueMap from(Object dataObject) throws ConversionException {
		Object fieldValue;
		
		Iterable<Object> fieldIterable;

		ValueMap fieldValueMap = new ValueMap();

		Class<?> c = dataObject.getClass();

		if (c.isAnnotationPresent(Templatable.class)) {

			for(Field field: c.getDeclaredFields()) {
				if (field.isAnnotationPresent(TemplateField.class)) {
					field.setAccessible(true);

					try {
						fieldValue = field.get(dataObject);

						if (!(fieldValue instanceof Iterable) && !field.getType().isArray()) {
					        Class<?> type = field.getType();
							// Simple  value
					        // TODO find a better way to do this. @See setField(...)
							if (type.isPrimitive() || isWrapperType(type) || type == String.class
									|| type == LocalDate.class || type == LocalDateTime.class || type == ZonedDateTime.class
									|| type == URL.class) {
								fieldValueMap.put(field.getName(), fieldValue);
							} else {
								ValueMap subValueMap = ValueMap.from(fieldValue);
								fieldValueMap.put(field.getName(), subValueMap);
							}
						} else {
							if (field.getType().isArray()) {

								// Unpack the array. Need to do this as:
								// a) the array needs to Iterable and for some reason they are not.
								// b) Just using simple casting does not seem to work for arrays
								// See https://stackoverflow.com/questions/8095016/unpacking-an-array-using-reflection
								List<Object> l = new ArrayList<Object>(Array.getLength(fieldValue));
								for (int i= 0; i < Array.getLength(fieldValue); i++) {
									l.add(Array.get(fieldValue,i));
								}
								fieldIterable = (Iterable<Object>) l;

							} else {
								// The field value is of type Iterable
								fieldIterable = (Iterable<Object>) fieldValue;
							}
							
							ValueMap fieldIterationMap;
							int i = 0;
							for (Object listEntry: fieldIterable) {
								fieldIterationMap = ValueMap.from(listEntry);
								fieldValueMap.put(field.getName() + "." + i++, fieldIterationMap);
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						String msg = "Unable to convert object to value map.";
						throw new ConversionException(msg, e);
					}
				}
			}
		} 

		return fieldValueMap;
    }
    
    /**  Creates a value map from the specified iterator.
     * <p>
     * For instance:
     * 
     * <pre>
     * {@code List<String> list = Arrays.asList(new String[]{"Asimov", "Clark", "Pohl"});
	 * ValueMap valueMap = ValueMap.from(list.iterator());
	 * }
	 * </pre>  
     *... will create a value map with the following entries:
     *<pre>
     * field name       value
     * ==========      =======
     *     0           "Asimov"
     *     1           "Clark"
     *     2           "Pohl"
     * </pre>
     *  
     * @param iterator An interator over objects
     * @throws ConversionException If the iterator cannot be converted to a value map
     * @return This value map
     */
    public static ValueMap from(Iterator<?> iterator) throws ConversionException {
    	ValueMap fieldValueMap = new ValueMap();   
    	int index = 0;
    	while (iterator.hasNext()) {
    		Object value = iterator.next();
    		Class<?> type = value.getClass();
    		// TODO find a better way to do this. @See Template.setField(...)
    		if (type.isPrimitive() || isWrapperType(type) || type == String.class
    				|| type == LocalDate.class || type == LocalDateTime.class || type == ZonedDateTime.class
    				|| type == URL.class) {
    			fieldValueMap.put(String.valueOf(index++), value);
    		} else {
    			ValueMap subValueMap = ValueMap.from(value);
    			fieldValueMap.put(String.valueOf(index++), subValueMap);
    		}

    	}

    	return fieldValueMap;

    }
    
    /**Creates an object from the specified class using the data in this value map.
     * 
     * @param objectClass The class of the object to be created
     * @return An object populated with the values in this value map
     * @throws ConversionException If the value map cannot be converted to an object 
     */
    public Object toObject(Class<?> objectClass) throws ConversionException {
		Object dataObject;
		
		//TODO what happens if the specified objectClass does not have a constructor with no parameters?
		try {
			dataObject = objectClass.getDeclaredConstructor().newInstance();  // Note: constructor can be private TODO check this
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			String msg = "Unable to instantiate an object of class" + objectClass.getName();
			throw new ConversionException(msg, e);
		}

		// Using the entries in the value map find the corresponding fields and set them.
		// TODO simple case first
		Set<String> fieldNames = this.fieldNames();

		for (String fieldName : fieldNames) {
			// Get the value
			if (!this.isValueMap(fieldName)) {
				Optional<Object> fieldValue = this.getValue(fieldName);

				Field field;
				try {
					field = dataObject.getClass().getDeclaredField(fieldName);
					if (field.getAnnotation(TemplateField.class) != null ) {
						setField(dataObject, field, fieldValue);

					} else {
						// If the field has not been annotated then silently ignore
					}

					//TODO check annotation
				} catch (NoSuchFieldException | SecurityException e) {
					String msg = "Field name " +  fieldName + " is unknown in class " + objectClass.getName();
					throw new ConversionException(msg, e);

				}

			} else {
				Field field;

				try {
					field = dataObject.getClass().getDeclaredField(fieldName);
					if (field.getAnnotation(TemplateField.class) != null ) {
						if (field.getType() == List.class) {
							setListField(dataObject, field, this.getValueMap(fieldName).orElse(ValueMap.empty()));
						}

					} else {
						// If the field has not been annotated then silently ignore
					}
				} catch (NoSuchFieldException | SecurityException e) {
					String msg = "Field name " +  fieldName + " is unknown in class " + objectClass.getName();
					throw new ConversionException(msg, e);
				}

			}
		}

		return dataObject;
    }
 
    private void setField(Object dataObject, Field field, Optional<Object> fieldValue) throws ConversionException {

		if (fieldValue.isEmpty()) return;  // TODO replace with checkArgument?
		String valStr = fieldValue.orElseThrow().toString();

		Class<?> fieldType = field.getType();

		field.setAccessible(true);

		try {

			if (fieldType.equals(String.class)) {
				field.set(dataObject, valStr);
			} else if (fieldType.equals(Integer.TYPE)) {
				int val = Integer.parseInt(valStr);
				field.setInt(dataObject, val);
			} else if (fieldType.equals(Integer.class)) {
				Integer val = Integer.parseInt(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Short.TYPE)) {
				short val = Short.parseShort(valStr);
				field.setShort(dataObject, val);
			} else if (fieldType.equals(Short.class)) {
				Short val = Short.parseShort(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Byte.TYPE)) {
				byte val = Byte.parseByte(valStr);
				field.setByte(dataObject, val);
			} else if (fieldType.equals(Byte.class)) {
				Byte val = Byte.parseByte(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Long.TYPE)) {
				long val = Long.parseLong(valStr);
				field.setLong(dataObject, val);
			} else if (fieldType.equals(Long.class)) {
				Long val = Long.parseLong(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Double.TYPE)) {
				double val = Double.parseDouble(valStr);
				field.setDouble(dataObject, val);
			} else if (fieldType.equals(Double.class)) {
				Double val = Double.parseDouble(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Float.TYPE)) {
				float val = Float.parseFloat(valStr);
				field.setFloat(dataObject, val);
			} else if (fieldType.equals(Float.class)) {
				Float val = Float.parseFloat(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Boolean.TYPE)) {
				boolean val = Boolean.parseBoolean(valStr);
				field.setBoolean(dataObject, val);
			} else if (fieldType.equals(Boolean.class)) {
				Boolean val = Boolean.parseBoolean(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Character.TYPE) ) {
				char val = valStr.charAt(0);
				field.setChar(dataObject, val);
			} else if (fieldType.equals(Character.class)) {
				Character val = Character.valueOf(valStr.charAt(0));
				field.set(dataObject, val);
			}
			// Dates are formatted according to ISO_LOCAL_DATE or ISO_LOCAL_DATE_TIME.
			  else if (fieldType.equals(LocalDate.class)) {
				LocalDate val = LocalDate.parse(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(LocalDateTime.class)) {
				LocalDateTime val = LocalDateTime.parse(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(ZonedDateTime.class)) {
				ZonedDateTime val = ZonedDateTime.parse(valStr);
				field.set(dataObject, val);
			}
			// Use standard string representation of URLs
			else if (fieldType.equals(URL.class)) {
				URL val = new URL(valStr);
				field.set(dataObject, val);
			} else {
				System.err.println("Type of " + field.getName() + " is unknown");
			}
		} catch (IllegalArgumentException | IllegalAccessException | MalformedURLException e) {
			String msg = "Unable to set field " + field.getName();
			throw new ConversionException(msg, e);
		}

	}

    private void setListField (Object dataObject, Field field, ValueMap valueMap) throws ConversionException {
    	checkArgument(field.getType() == List.class, "The specified field name %s is not of type %s", field.getName(), List.class.getName());


    	//Class<?> fieldType = field.getType();
    	field.setAccessible(true);

    	// Determine the type of list object in the data object ?
    	Type genericType = field.getGenericType();

    	// Extract the parameterised type name
    	String paraTypeName = Splitter.on('<')
    			.trimResults(CharMatcher.is('>'))
    			.omitEmptyStrings()
    			.splitToList(genericType.getTypeName()).get(1);

    	// Now construct data objects for that type and fill them out with the data in the value map entries.  
    	List<ValueMap> vmList = valueMap.getValueMaps();
    	for (ValueMap vmEntry : vmList) {
    		try {
    			Class<?> paramClass = Class.forName(paraTypeName); //TODO move outside try block

    			// Now construct a data object of paramClass from the value map 
    			Object listEntryDataObject = vmEntry.toObject(paramClass);

    			@SuppressWarnings("unchecked")
    			List<Object> dataObjectList = (List<Object>)field.get(dataObject);

    			dataObjectList.add(listEntryDataObject)	;	


    		} catch (ConversionException e) {
    			//Pass this exception up the calling stack 
    			throw e;
    		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException  e) {
    			String msg = "Unable to set field " + field.getName();
    			throw new ConversionException(msg, e);
    		} 

    	}
    }

}
