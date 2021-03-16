package semplate;

//import static org.junit.Assume.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

class TemplateBasicTest {
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
		TestUtilities.copyFromResource(templateFileName, templateFile);
		/*
		 * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		 * InputStream in = classLoader.getResourceAsStream(templateFileName);
		 * Files.copy(in, templateFile);
		 */
		assertTrue(Files.exists(templateFile));
		
			
	}

	@AfterEach
	void tearDown() throws Exception {
	  fileSystem.close();
	}


	@Test
	void testConfigWithPath() throws IOException, ReadException {
		Template t = new Template();
		assumeTrue(t != null);
		
		t.config(templateFile);

		assertTrue(t.getTemplatePath().toString().equals("/templates/" + templateFileName));

		assertEquals("<!--", t.getCommentStartDelimiter().orElse(""));
		assertEquals("-->", t.getCommentEndDelimiter().orElse("")); 
	}
	
	
	@Test
	void testConfigWithName(@TempDir Path tempDir) throws IOException, ReadException{
		Template t = new Template();
		assumeTrue(t != null);
		
        Path sourceFile = tempDir.resolve("simple_expected.md");
		
	    TestUtilities.copyFromResource("simple_expected.md", sourceFile);
		
		String templateFileName = sourceFile.toString();
				
		t.config(templateFileName);		
		
		assertTrue(t.getCommentStartDelimiter().get().equals("<!--"));
		assertTrue(t.getCommentEndDelimiter().get().equals("-->"));

	}
	
	@Test
	void testTemplateHasNoCommentDirective(@TempDir Path tempDir) throws IOException, ReadException{
		Template t = new Template();
		assumeTrue(t != null);
		
        Path sourceFile = tempDir.resolve("no_comment_directive.md");
		
	    TestUtilities.copyFromResource("no_comment_directive.md", sourceFile);
		
		String templateFileName = sourceFile.toString();
				
		assertThrows(ReadException.class, () -> t.config(templateFileName));		
		
		
	}
	

}
