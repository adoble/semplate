package semplate;

//import static org.junit.Assume.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
	final static String listTemplateFileName = "list_template.md";
	
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
	void testGenerateSimple() throws Exception{
		
		Template template = new Template();
		assumeTrue(template != null);
		
		
		template.config(templateFile);
				
		Path outputPath = templatesPath.resolve("the_republic.md");
		
		template.generate(work, outputPath);
		
		assertTrue(Files.exists(outputPath));
		
		String actualContents = Files.lines(outputPath).collect(Collectors.joining());
		
		String resourceFileName = "simple_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  // Expected file as the same name as the resource 
		
		
		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		
		 String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
		
		assertEquals(expectedContents, actualContents);
	}
	
	@Test
	void testGenerateList() throws Exception {
		
		// Copy into the mock file system the template file we are using from the resources
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

		assertTrue(Files.exists(outputPath));
		
	
		String actualContents = Files.lines(outputPath).collect(Collectors.joining());
			
		
		String resourceFileName = "list_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  // Expected file as the same name as the resource 
		
		
		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		
		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
					
		//assertThat(actualContents, is(expectedContents));
		assertEquals(expectedContents, actualContents);
	}
	
	@Test
	void testGenerateFromArray() throws Exception {
		String arrayTemplateFileName = "array_template.md";
		
		// Copy into the mock file system the template file we are using from the resources
		Path arrayTemplateFile = templatesPath.resolve(arrayTemplateFileName);
		TestUtilities.copyFromResource(arrayTemplateFileName, arrayTemplateFile);  
		
		assertTrue(Files.exists(arrayTemplateFile));
		Template template = new Template();
		assumeTrue(template != null);
				
		template.config(arrayTemplateFile);
				
		// Set up a templatable object that uses an array. 		
		References references = new References(3);
		references.add(new Reference("Figuring the Phallogocentric Argument with Respect to the Classical Greek Philosophical Tradition", 
				                     new URL("http://kenstange.com/nebula/feat013/feat013.html")));
		references.add(new Reference("A History of Mathematics", 
				                     new URL("https://archive.org/details/historyofmathema00boye")));
		references.add(new Reference("Philosophy and Dialogue: Plato's Unwritten Doctrines from a Hermeneutical Point of View", 
                                     new URL("http://www.bu.edu/wcp/Papers/Anci/AnciRodr.htm")));

		
			
		Path outputPath = fileSystem.getPath("array_actual.md");
				
		template.generate(references, outputPath);
		
		assertTrue(Files.exists(outputPath));
		
		String actualContents = "";
		
		actualContents = Files.lines(outputPath).collect(Collectors.joining());
		
		String expectedContents = "";
		String resourceFileName = "array_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  // Expected file as the same name as the resource 
		
		
		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		
		expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
					
		assertEquals(expectedContents, actualContents);
	
	}
	
	@Disabled
	@Test
	public void testGenerateComplex() {
		//TODO test with linked classes
		fail("Nor yet implemented");
	}
}
