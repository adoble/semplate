package templato;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class TemplateTest {
	final static String templateFileName = "simple_template.md";
	
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
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in  = classLoader.getResourceAsStream(templateFileName);
		Files.copy(in, templateFile);
		assertTrue(Files.exists(templateFile));
		
		

	}

	@AfterEach
	void tearDown() throws Exception {
	}


	@Test
	void testConfigWithPath() {
		Template t = new Template();
		assertNotNull(t);
		
		try {
			t.config(templateFile);
		} catch (IOException e) {
			fail("Unxpected exception: " + e.getMessage());
		}
		
		assertTrue(t.getTemplatePath().toString().equals("/templates/" + templateFileName));
		
		assertTrue(t.getCommentStartDelimiter().equals("<!--"));
		assertTrue(t.getCommentEndDelimiter().equals("-->")); 
		
	
	}
	
	
	@Test
	void testConfigWithStream() {
		Template t = new Template();
		assertNotNull(t);
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(templateFileName);
		try {
			t.config(in);
		} catch (IOException e) {
			fail("Unxpected exception: " + e.getMessage());
		}

		assertTrue(t.getCommentStartDelimiter().equals("<!--"));
		assertTrue(t.getCommentEndDelimiter().equals("-->"));

	}

}
