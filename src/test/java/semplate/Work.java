package semplate;

import java.net.URL;

import semplate.annotations.*;

@Templatable
public class Work {
	
	@TemplateField
	private String title;
	
	@TemplateField
	private String author;
	
	@TemplateField
	private String translator;
	
	@TemplateField
	private String source;
	
	@TemplateField
	private URL sourceLink;
	
	@TemplateField
	private int id;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTranslator() {
		return translator;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public URL getSourceLink() {
		return sourceLink;
	}

	public void setSourceLink(URL sourceLink) {
		this.sourceLink = sourceLink;
	}
	
	

}
