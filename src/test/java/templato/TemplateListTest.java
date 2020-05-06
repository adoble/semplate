package templato;

import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.*;

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

class TemplateListTest {
	
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
		// TODO use TestUtilites
		templateFile = templatesPath.resolve(templateFileName);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in  = classLoader.getResourceAsStream(templateFileName);
		Files.copy(in, templateFile);
		assertTrue(Files.exists(templateFile));
	}

	@Test
	void testGenerateList() {
		Template template = new Template();
		assumeNotNull(template);
		
		try {
			template.config(templateFile);
		} catch (IOException e) {
			fail("Unxpected exception: " + e.getMessage());
		}
		
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
		
		
		TestUlilities.copyFromResource(resourceFileName, expectedFile);
		
		
		try (Stream<String> stream = Files.lines(expectedFile)) {
			
			expectedContents = stream.collect(Collectors.joining());
		} catch (IOException e) {
			fail(e.getMessage());
		}	
	
				
		//assertThat(actualContents, is(expectedContents));
		assertEquals(expectedContents, actualContents);
	}
	
	@Disabled
	@Test
	public void testGenerateComplex() {
		//TODO test with linked classes
		fail("Nor yet implemented");
	}
	

}
