package templato.valuemap;

public class ValueMapEntry {

	private String fieldName; 
	private Object fieldValue;

	public ValueMapEntry() {
		this.fieldName = "";
		this.fieldValue= "";
	}

	public ValueMapEntry(String fieldName, Object fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue= fieldValue;
	}

	public final String getFieldName()  { return this.fieldName; }
	public final Object getFieldValue() { return this.fieldValue; }

	public final void setFieldName(String fieldName)   { this.fieldName = fieldName;  }
	public final void setFieldValue(Object fieldValue) { this.fieldValue = fieldValue; }



}
