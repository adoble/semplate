package semplate;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;

@Templatable
public class Link {
	
	@TemplateField
	int id;
	
	@TemplateField
	Linked reference;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Linked getReference() {
		return reference;
	}
	public void setReference(Linked reference) {
		this.reference = reference;
	} 


}
