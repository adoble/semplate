package semplate;

import java.util.*;

import semplate.annotations.*;


@Templatable
public class Works {
	@TemplateField
	private int id;
	
	
	@TemplateField
	private String title;
	
	@TemplateField
	private String author;

	@TemplateField
	List<Reference> references = new ArrayList<Reference>();
	
	
public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	
	
	public void setAuthor(String author) {
		this.author = author;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
		
	}


	public void addReference(Reference reference) {
		references.add(reference);
		
	}
	
	public Reference getReference(int index) {
		return references.get(index);
	}
	
	public int numberReferences() {
		return references.size();
	}

}
