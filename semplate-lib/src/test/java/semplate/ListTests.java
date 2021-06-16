package semplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

class ListTests {
	
	final static String listTemplateFileName = "list_template.md";

	private static FileSystem fileSystem;
	private Path rootPath;
	private Path templatesPath;

	private Path templateFile;
	
	@BeforeEach
	void setUp() throws Exception {
		// Set up the mock file system
		fileSystem = Jimfs.newFileSystem(Configuration.unix());

		rootPath = fileSystem.getPath("/files");  // Test files here
		Files.createDirectory(rootPath);

		templatesPath = fileSystem.getPath("/templates");  // Template files here
		Files.createDirectory(templatesPath);

		//Copy a test template from the resources to the mock file system
		// TODO use TestUtilites
		
//		templateFile = templatesPath.resolve(templateFileName);
//		TestUtilities.copyFromResource(templateFileName, templateFile);  
//		
//		assertTrue(Files.exists(templateFile));
	}
	

	

	
	@Test
	public void testReadList() throws Exception { 
		templateFile = templatesPath.resolve(listTemplateFileName);
		TestUtilities.copyFromResource(listTemplateFileName, templateFile);  
		
		assertTrue(Files.exists(templateFile));
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
		
		Works works = new Works();
		works.setTitle("The Works of Plato");
		works.setAuthor("Plato");
		works.addReference(new Reference("Apology", new URL("https://en.wikisource.org/wiki/Apology_%28Plato%29")));
		works.addReference(new Reference("Charmides", new URL("https://en.wikisource.org/wiki/Charmides_%28Plato%29")));
		works.addReference(new Reference("The Republic", new URL("https://en.wikisource.org/wiki/The_Republic_of_Plato")));
			
		Path outputPath = fileSystem.getPath("list_actual.md");
		
		template.generate(works, outputPath);
		
		assumeTrue(Files.exists(outputPath));
		
		
		// Now read in the file and reconstruct the object 
		//Works rWorks = (Works) template.read(Works.class,  outputPath);
		Works rWorks = (Works) SemanticReader.with(Works.class).usingFile(outputPath).read();
		
		assertEquals("Plato", rWorks.getAuthor());
		assertEquals("The Works of Plato", rWorks.getTitle());
		
		assertEquals(3, rWorks.numberReferences());
		assertEquals("Apology", rWorks.getReference(0).getTitle());
		assertEquals("https://en.wikisource.org/wiki/Apology_%28Plato%29", rWorks.getReference(0).getLink().toString());
		assertEquals("Charmides", rWorks.getReference(1).getTitle());
		assertEquals("https://en.wikisource.org/wiki/Charmides_%28Plato%29", rWorks.getReference(1).getLink().toString());
		assertEquals("The Republic", rWorks.getReference(2).getTitle());
		assertEquals("https://en.wikisource.org/wiki/The_Republic_of_Plato", rWorks.getReference(2).getLink().toString());
		
		
	}
	

}
