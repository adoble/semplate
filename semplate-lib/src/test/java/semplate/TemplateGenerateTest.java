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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

class TemplateGenerateTest {
	final static String templateFileName = "simple_template.md";
	
	private static FileSystem fileSystem;
	private Path rootPath;
	private Path templatesPath;
	
	private Path templateFile;
	
	// Test data object
	Work work;
	

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
		
		// Set up the test data object
		work = new Work();
		work.setAuthor("Plato");
		work.setTitle("The Republic");
		work.setTranslator("Benjamin Jowett");
		work.setSource("Wikisource");
		work.setSourceLink(new URL("https://en.wikisource.org/wiki/The_Republic"));
		work.setId(4711);
		
	}

	@AfterEach
	void tearDown() throws Exception {
	  fileSystem.close();
	}


	
	@Test 
	void testGenerateSimple() {
		
		Template template = new Template();
		assumeTrue(template != null);
		
		try {
			template.config(templateFile);
		} catch (IOException e) {
			fail("Unxpected exception: " + e.getMessage());
		}
		
		
		Path outputPath = templatesPath.resolve("the_republic.md");
		
		try {
			template.generate(work, outputPath);
		}
		catch (IOException e) {
			fail(e.getMessage());
		}

		assertTrue(Files.exists(outputPath));
		
		
		String actualContents = "";
		try (Stream<String> stream = Files.lines(outputPath)) {
            actualContents = stream.collect(Collectors.joining());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
			
		String expectedContents = "";
		String resourceFileName = "simple_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  // Expected file as the same name as the resource 
		
		
		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		
		
		try (Stream<String> stream = Files.lines(expectedFile)) {
			
			expectedContents = stream.collect(Collectors.joining());
		} catch (IOException e) {
			fail(e.getMessage());
		}	
	
				
		//assertThat(actualContents, is(expectedContents));
		assertEquals(expectedContents, actualContents);
		

	}
	

	

}