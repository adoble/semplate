package semplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

class TemplateListTest {
	
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
		try {
			works.addReference(new Reference("Apology", new URL("https://en.wikisource.org/wiki/Apology_(Plato)")));
			works.addReference(new Reference("Charmides", new URL("https://en.wikisource.org/wiki/Charmides_(Plato)")));
			works.addReference(new Reference("The Republic", new URL("https://en.wikisource.org/wiki/The_Republic_of_Plato")));
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}

			
		Path outputPath = fileSystem.getPath("list_actual.md");
		
		try {
			template.generate(works, outputPath);
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
		String resourceFileName = "list_expected.md";
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
		works.addReference(new Reference("Apology", new URL("https://en.wikisource.org/wiki/Apology_(Plato)")));
		works.addReference(new Reference("Charmides", new URL("https://en.wikisource.org/wiki/Charmides_(Plato)")));
		works.addReference(new Reference("The Republic", new URL("https://en.wikisource.org/wiki/The_Republic_of_Plato")));
			
		Path outputPath = fileSystem.getPath("list_actual.md");
		
		template.generate(works, outputPath);
		
		assumeTrue(Files.exists(outputPath));
		
		// Now read in the file and reconstruct the object 
		Works rWorks = (Works) template.read(Works.class,  outputPath);
		
		assertEquals("Plato", rWorks.getAuthor());
		assertEquals("The Works of Plato", rWorks.getTitle());
		
		assertEquals(3, rWorks.numberReferences());
		assertEquals("Apology", rWorks.getReference(0).getTitle());
		assertEquals("https://en.wikisource.org/wiki/Apology_(Plato)", rWorks.getReference(0).getLink().toString());
		assertEquals("Charmides", rWorks.getReference(1).getTitle());
		assertEquals("https://en.wikisource.org/wiki/Charmides_(Plato)", rWorks.getReference(1).getLink().toString());
		assertEquals("The Republic", rWorks.getReference(2).getTitle());
		assertEquals("https://en.wikisource.org/wiki/The_Republic_of_Plato", rWorks.getReference(2).getLink().toString());
		
		
	}
	

}
