package semplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class TestSemanticReader {

	private static FileSystem fileSystem;
	private Path rootPath;

	@BeforeEach
	void setUp() throws Exception {
		// Set up the mock file system
		fileSystem = Jimfs.newFileSystem(Configuration.unix());

		rootPath = fileSystem.getPath("/files");  // Test files here
		Files.createDirectory(rootPath);
	}

	@AfterEach
	void tearDown() throws Exception {
		fileSystem.close();
	}

	@Test
	void testReadChainSimple() throws Exception {
		Path sourceFile = rootPath.resolve("simple_expected.md");
		
		TestUtilities.copyFromResource("simple_expected.md", sourceFile);
		
		Work workExpected = (Work) SemanticReader.with(Work.class)
				                                 .usingFile(sourceFile)
				                                 .read();   
		
		assertNotNull(workExpected);
		
		assertEquals("Plato", workExpected.getAuthor());
		assertEquals("The Republic", workExpected.getTitle());
		assertEquals("Benjamin Jowett", workExpected.getTranslator());
		assertEquals("Wikisource", workExpected.getSource());
		assertEquals(4711, workExpected.getId());
	    
	    URL sourceLink = workExpected.getSourceLink();
	    assertNotNull(sourceLink);
	    assertEquals("https://en.wikisource.org/wiki/The_Republic", sourceLink.toString());
	    
	}

	
	@Test
	void testReadChainWithList() throws Exception {

		Path sourceFile = rootPath.resolve("list_expected.md");

		TestUtilities.copyFromResource("list_expected.md", sourceFile);

		Works works = (Works) SemanticReader.with(Works.class)
				.usingFile(sourceFile)
				.read();   


		assertNotNull(works);

		assertEquals("Plato", works.getAuthor());
		assertEquals("The Works of Plato", works.getTitle());

		assertTrue(works.numberReferences() == 3);

		Reference r;       
		r = works.getReference(0);
		assertEquals("Apology", r.getTitle() );
		assertEquals("https://en.wikisource.org/wiki/Apology_%28Plato%29", r.getLink().toString());

		r = works.getReference(1);
		assertEquals("Charmides", r.getTitle());
		assertEquals("https://en.wikisource.org/wiki/Charmides_%28Plato%29", r.getLink().toString());

		r = works.getReference(2);
		assertEquals("The Republic", r.getTitle());
		assertEquals("https://en.wikisource.org/wiki/The_Republic_of_Plato", r.getLink().toString());
	}
	
}
