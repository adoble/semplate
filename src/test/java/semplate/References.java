package semplate;


import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;

@Templatable
public class References {

	static final int defaultSize = 10;

	@TemplateField
	Reference[] references;
	
	int index = 0;


	public References() {
		this.references = new Reference[defaultSize];
	}


	public References(int size) {
		this.references = new Reference[size];
	}


	public int add(Reference ref) {
		
		if (index < references.length ) {
			references[index++] = ref;
			return index - 1;
		} else {
			return -1;
		}

   
	}


}
