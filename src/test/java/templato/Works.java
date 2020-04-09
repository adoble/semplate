package templato;

import java.util.*;

import templato.annotations.*;


@Templatable
public class Works {
	@TemplateField
	private String title;
	
	@TemplateField
	private String author;
	
	@TemplateList
	List<Reference> references = new ArrayList<Reference>();
	

	public String getAuthor() {
		return author;
	}
	
	
	public void setAuthor(String author) {
		this.author = author;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String string) {
		// TODO Auto-generated method stub
		
	}


	public void addReference(Reference reference) {
		references.add(reference);
		
	}

}
