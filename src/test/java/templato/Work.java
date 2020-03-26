package templato;

import java.net.URL;

import templato.annotations.*;

@Templatable
public class Work {
	
	@TemplateField
	private String title;
	
	private String author;
	
	private String translator;
	
	private String source;
	
	private URL sourceLink;

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
