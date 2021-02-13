package semplate;

import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

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
	  fileSystem.close();
	}


	@Test
	void testConfigWithPath() {
		Template t = new Template();
		assumeNotNull(t);
		
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
		assumeNotNull(t);
		
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
	
	@Test 
	void testGenerateSimple() {
		
		fail("Test not implmented");
		Template template = new Template();
		assumeNotNull(template);
		
		try {
			template.config(templateFile);
		} catch (IOException e) {
			fail("Unxpected exception: " + e.getMessage());
		}
		
		Work work = new Work();
		work.setAuthor("Plato");
		work.setTitle("The Republic");
		work.setTranslator("Benjamin Jowett");
		work.setSource("Wikisource");
		
				
		try {
			work.setSourceLink(new URL(" https://en.wikisource.org/wiki/The_Republic"));
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		
		Path outputPath = templatesPath.resolve("the_republic.md");
		template.generate(work, outputPath);

		assertTrue(Files.exists(outputPath));

		try (Stream<String> stream = Files.lines(outputPath)) {
            //TODO stream.
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		//count = Files.lines(file).filter(s -> s.contains(lookFor)).count();

	}

}
