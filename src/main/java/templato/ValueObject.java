package templato;

import java.util.List;
import java.util.Map;

// TODO THIS IS A STUB

public class ValueObject {
	String name;
	Object object;  // The value of a field object 
		
	// String is the name of the list entry attibute, object id the value of this attibute
	Map<String, ValueObject> listValues; 
	
	
	public boolean isField() {
		return true;
	}
	
	public boolean isList() {
		return false;
	}
	
	//getValues(String listName, attibuteName)
	
	
	
	
	
	
	

}
