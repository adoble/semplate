package semplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class TestSemanticWriter {
	
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
	void testWith() {
		Work w = new Work();

		SemanticWriter semanticWriter = SemanticWriter.with(w);

		Work x = (Work) semanticWriter.getDataObject();

		assertEquals(w, x);

	}

	@Test 
	void testUsingTemplate() throws Exception {
		String templateFileName = "simple_template.md";
		Path templateFile = rootPath.resolve(templateFileName);
		TestUtilities.copyFromResource(templateFileName, templateFile);
		
		Work workDataObject = new Work();
		

		SemanticWriter semanticWriter = SemanticWriter.with(workDataObject);
				
		SemanticTemplateWriter semanticTemplateWriter = semanticWriter.usingTemplate(templateFile);
		
		assertNotNull(semanticTemplateWriter);
		
	}
	
	// Using @TempDir as the file name as SemanticWriter using the default file system 
	// to resolve names. 
	@Test 
	void testUsingTemplateName(@TempDir Path tempDir) throws Exception {
		String templateFileName = "simple_template.md";
		Path templateFile = tempDir.resolve(templateFileName);
		TestUtilities.copyFromResource(templateFileName, templateFile);
		
		Work workDataObject = new Work();
		

		SemanticWriter semanticWriter = SemanticWriter.with(workDataObject);
		
		String fullTemplateFileName = templateFile.toString();
				
		SemanticTemplateWriter semanticTemplateWriter = semanticWriter.usingTemplate(fullTemplateFileName);
		
		assertNotNull(semanticTemplateWriter);
		
	}
	
	@Test
	void testErrorUsingTemplateName() throws Exception {

		SemanticWriter semanticWriter = SemanticWriter.with(new Work());
		assertThrows(ReadException.class, () -> {
			semanticWriter.usingTemplate("rubbish name");
		});
	}
	
	@Test 
	void testGenerateChainWithBasicTemplate() throws Exception {
		Path templateFile = rootPath.resolve("simple_template.md");
		TestUtilities.copyFromResource("simple_template.md", templateFile);
		
	    Path outputFile = rootPath.resolve("simple_actual.md");
	    
	    Path expectedFile = rootPath.resolve("simple_expected.md");
	    TestUtilities.copyFromResource("simple_expected.md", expectedFile);
		
		Work work = new Work();
		work.setAuthor("Plato");
		work.setTitle("The Republic");
		work.setTranslator("Benjamin Jowett");
		work.setSource("Wikisource");
		work.setSourceLink(new URL("https://en.wikisource.org/wiki/The_Republic"));
		work.setId(4711);
		
		SemanticWriter.with(work).usingTemplate(templateFile).write(outputFile);
		
        String actualContents = Files.lines(outputFile).collect(Collectors.joining());
		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
		
		assertEquals(expectedContents, actualContents);
		
	}
	
	@Test
	void testGenerateChainWithList() throws Exception {
		Path templateFile = rootPath.resolve("list_template.md");
		TestUtilities.copyFromResource("list_template.md", templateFile);
		
	    Path outputFile = rootPath.resolve("list_actual.md");
	    
	    Path expectedFile = rootPath.resolve("list_expected.md");
	    TestUtilities.copyFromResource("list_expected.md", expectedFile);
		
		
		Works works = new Works();
		works.setTitle("The Works of Plato");
		works.setAuthor("Plato");

		works.addReference(new Reference("Apology", new URL("https://en.wikisource.org/wiki/Apology_%28Plato%29")));
		works.addReference(new Reference("Charmides", new URL("https://en.wikisource.org/wiki/Charmides_%28Plato%29")));
		works.addReference(new Reference("The Republic", new URL("https://en.wikisource.org/wiki/The_Republic_of_Plato")));

		
		SemanticWriter.with(works).usingTemplate(templateFile).write(outputFile);
		
		
		String actualContents = Files.lines(outputFile).collect(Collectors.joining());
		
		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
					
		assertEquals(expectedContents, actualContents);
	}
	
	@Test
	void testUpdateChainWithSimpleMardown() throws Exception {
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
		
		//new Template().update(updatedWork, sourceFile, outputFile);
        SemanticWriter.with(updatedWork)                 
                         .usingFile(sourceFile)     
                         .write(outputFile);
        
        
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
	void testUpdateChainWithListMarkdown () throws Exception {
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
        
        
        SemanticWriter.with(updatedWorks).usingFile(sourceFile).write(outputFile);
        
        String resourceFileName = "list_updated_expected.md";
		Path expectedFile = fileSystem.getPath(resourceFileName);  
		
        TestUtilities.copyFromResource(resourceFileName, expectedFile);
		assumeTrue(Files.exists(expectedFile));
		
		String expectedContents = Files.lines(expectedFile).collect(Collectors.joining());
		
	    String actualContents = Files.lines(outputFile).collect(Collectors.joining());
				
		assertEquals(expectedContents, actualContents);
	}
	

}
