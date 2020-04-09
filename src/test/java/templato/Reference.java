package templato;

import java.net.URL;

import templato.annotations.Templatable;
import templato.annotations.TemplateField;

@Templatable
public class Reference {
	@TemplateField 
	private String title;
	
	@TemplateField
	private URL link;

	public Reference(String title, URL link) {
		super();
		this.title = title;
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}

}
