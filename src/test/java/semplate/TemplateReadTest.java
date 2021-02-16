package semplate;


import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

public class TemplateReadTest {
	final static String templateFileName = "list_template.md";
	
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
		templateFile = templatesPath.resolve(templateFileName);
		TestUtilities.copyFromResource(templateFileName, templateFile);
		assertTrue(Files.exists(templateFile));
		
	}

	@AfterEach
	void tearDown() throws Exception {
	  fileSystem.close();
	}
	
	@Disabled
	@Test
	void testRead() throws Exception {
		
		Works works = null;
		
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
		Path sourceFile = templatesPath.resolve("list_expected.md");
		
		TestUtilities.copyFromResource("list_expected.md", sourceFile);
		
        works = (Works) template.read(Works.class, sourceFile);
        
        assertNotNull(works);
        
        assertEquals("Plato", works.getAuthor());
        assertEquals("The Works of Plato", works.getTitle());
        
        assertTrue(works.numberReferences() == 3);
        
        Reference r;       
        r = works.getReference(0);
        assertEquals("Apology", r.getTitle() );
        assertEquals("https://en.wikisource.org/wiki/Apology_(Plato)", r.getLink().toString());
        
        r = works.getReference(1);
        assertEquals("Charmides", r.getTitle());
        assertEquals("https://en.wikisource.org/wiki/Charmides_(Plato)", r.getLink().toString());
        
        r = works.getReference(2);
        assertEquals("The Republic", r.getTitle());
        assertEquals("https://en.wikisource.org/wiki/The_Republic_of_Plato", r.getLink().toString());
        
        	 
        }
        
        
		
}

