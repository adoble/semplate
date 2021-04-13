package semplate;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;

@Templatable
public class Linked {
	
	@TemplateField
	int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


}
