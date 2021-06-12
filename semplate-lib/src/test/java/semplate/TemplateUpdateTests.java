package semplate;


import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;

public class TemplateUpdateTests {
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
		assertTrue(Files.exists(templateFile));

	}

	@AfterEach
	void tearDown() throws Exception {
	  fileSystem.close();
	}

	@Test
	@Deprecated
	public void testCreationOfTempFile(@TempDir Path tempDir) throws Exception {
		String expectedContents = "Thou gazest on the stars, my star!\n"
		                   		+ "Ah! would that I might be\n"
		                   		+ "Myself those skies with myriad eyes,\n"
		                   		+ "That I might gaze on thee.";

		// Create a file in the temp dir
		Path sourceFile = Files.createFile(tempDir.resolve("test-source-file.txt"));
		// Fill it with some text
		Files.writeString(sourceFile, expectedContents);

		Template template = new Template();
		// Invoke the private method copyToTempFile  --> Have to use reflection
		Method method = Template.class.getDeclaredMethod("copyToTempFile", Path.class);
		method.setAccessible(true);
		Path tempPath = (Path) method.invoke(template, sourceFile);

		assertTrue(tempPath.toFile().exists());

		// Compare the contents
		String actualContents = Files.lines(tempPath).collect(Collectors.joining("\n"));

		assertEquals(expectedContents, actualContents);

	}

	// Test update with no lists
	@Test
	void testSimpleUpdate() throws Exception {

		Path sourceFile = rootPath.resolve("simple_expected.md");

		TestUtilities.copyFromResource("simple_expected.md", sourceFile);

		Path outputFile = rootPath.resolve("simple_updated.md");


        // Updated data in the data object
        Work updatedWork = new Work();
        updatedWork.setAuthor("Plato The Fraudulant");
        updatedWork.setTitle("The Dictatorship");
        updatedWork.setTranslator("Old Ben");
        updatedWork.setSource("Wikipedia");
        updatedWork.setSourceLink(new URL("https://en.wikipedia.org/wiki/Academic_dishonesty"));
        updatedWork.setId(4713);

		new Template().update(updatedWork, sourceFile, outputFile);

		String expectedContents = "";
		String resourceFileName = "simple_updated_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  // Expected file as the same name as the resource

		TestUtilities.copyFromResource(resourceFileName, expectedFile);

		assumeTrue(Files.exists(expectedFile));
		expectedContents = Files.lines(expectedFile).collect(Collectors.joining());

	    String actualContents = Files.lines(outputFile).collect(Collectors.joining());

		assertEquals(expectedContents, actualContents);


        }

	@Test
	void testListUpdate() throws Exception {
		Path sourceFile = rootPath.resolve("list_expected.md");
		TestUtilities.copyFromResource("list_expected.md", sourceFile);

		Path outputFile = rootPath.resolve("list_output_file.md");

		// Updated data in the data object
		Works updatedWorks = new Works();
		updatedWorks.setId(1960);
		updatedWorks.setAuthor("Stan Lee");
		updatedWorks.setTitle("The Comic Books of Stan Lee");

        updatedWorks.addReference(new Reference("The Amazing Spider-Man",
                new URL("https://en.wikipedia.org/wiki/The_Amazing_Spider-Man" )));
        updatedWorks.addReference(new Reference("Journey into Mystery",
                new URL("https://en.wikipedia.org/wiki/Journey_into_Mystery" )));
        updatedWorks.addReference(new Reference("Ravage 2099",
                new URL("https://en.wikipedia.org/wiki/Ravage_2099" )));


        new Template().update(updatedWorks, sourceFile, outputFile);

        String resourceFileName = "list_updated_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);

        TestUtilities.copyFromResource(resourceFileName, expectedFile);
		assumeTrue(Files.exists(expectedFile));

		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());

	    String actualContents = Files.lines(outputFile).collect(Collectors.joining());

		assertEquals(expectedContents, actualContents);
	}

	@Test
	void testListRemovalUpdate() throws Exception {
		Path sourceFile = rootPath.resolve("list_expected.md");
		TestUtilities.copyFromResource("list_expected.md", sourceFile);

		Path outputFile = rootPath.resolve("list_output_file.md");

		Template t = new Template();
		Works works = (Works) t.read(Works.class, sourceFile);

		// Now update the works by removing an entry in the middle
		works.removeReference(1);

        // Now update
        t.update(works, sourceFile, outputFile);

        String resourceFileName = "list_removal_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);

        TestUtilities.copyFromResource(resourceFileName, expectedFile);
		assumeTrue(Files.exists(expectedFile));

		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());

	    String actualContents = Files.lines(outputFile).collect(Collectors.joining());

		assertEquals(expectedContents, actualContents);
	}

	@Test
	void testListAdditionUpdate() throws Exception {
		Path sourceFile = rootPath.resolve("list_expected.md");
		TestUtilities.copyFromResource("list_expected.md", sourceFile);

		Path outputFile = rootPath.resolve("list_output_file.md");

		Template t = new Template();
		Works works = (Works) t.read(Works.class, sourceFile);

		// Now update the works by adding two entries
		works.addReference(new Reference("The Symposium", new URL("https://en.wikisource.org/wiki/Symposium_%28Plato%29")));
		works.addReference(new Reference("The Gorgias", new URL("https://en.wikisource.org/wiki/Gorgias")));

		System.out.println(works);
		// Now update
		t.update(works, sourceFile, outputFile);

		String resourceFileName = "list_addition_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);

		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		assumeTrue(Files.exists(expectedFile));

		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());

		String actualContents = Files.lines(outputFile).collect(Collectors.joining());

		assertEquals(expectedContents, actualContents);
	}

	@Test
	void testListCompleteRemovalUpdate() throws Exception {
		Path sourceFile = rootPath.resolve("list_expected.md");
		TestUtilities.copyFromResource("list_expected.md", sourceFile);

		Path outputFile = rootPath.resolve("list_output_file.md");

		Template t = new Template();
		Works works = (Works) t.read(Works.class, sourceFile);

		// Now delete all the entries
		while (works.numberReferences() > 0 ) {
			works.removeReference(0);
		}

		// Now update
        t.update(works, sourceFile, outputFile);

		String resourceFileName = "list_complete_removal_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);

		TestUtilities.copyFromResource(resourceFileName, expectedFile);
		assumeTrue(Files.exists(expectedFile));

		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());

		String actualContents = Files.lines(outputFile).collect(Collectors.joining());

		assertEquals(expectedContents, actualContents);
	}

}
