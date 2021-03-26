package semplate;


import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import semplate.Template;
import semplate.valuemap.ValueMap;

public class TemplateReadTest {
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
		templateFile = templatesPath.resolve(templateFileName);
		TestUtilities.copyFromResource(templateFileName, templateFile);
		assertTrue(Files.exists(templateFile));
		
	}

	@AfterEach
	void tearDown() throws Exception {
	  fileSystem.close();
	}
	
	@Test
	void testReadSimple() throws Exception{
			
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
		Path sourceFile = templatesPath.resolve("simple_expected.md");
		
		TestUtilities.copyFromResource("simple_expected.md", sourceFile);
		
		Work workExpected = (Work) template.read(Work.class, sourceFile);
		
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
	void testReadList() throws Exception {
		
		Works works = null;
		
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
		Path sourceFile = templatesPath.resolve("list_expected.md");
		
		TestUtilities.copyFromResource("list_expected.md", sourceFile);
		
	    works = (Works) template.read(Works.class, sourceFile);
	    
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

	/** Test that an error occurs if the markup file does not exist. 
	 * 
	 */
	@Test
	void testReadMarkupError() throws IOException, ReadException {
		
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
        Path sourceFile = templatesPath.resolve("non-existent.md");
        
        assertThrows(ReadException.class, () -> {
        	template.read(Work.class, sourceFile);
        });
		
	}
	
	@Test
	void testUsingInvalidClass () throws IOException, ReadException {
		Template template = new Template();
		assumeTrue(template != null);
		
		template.config(templateFile);
		
        Path sourceFile = templatesPath.resolve("simple_expected.md");
		
		TestUtilities.copyFromResource("simple_expected.md", sourceFile);
        
        assertThrows(ReadException.class, () -> {
        	template.read(NonValidClass.class, sourceFile);
        });
	}
	
	@Test
	void tryIt() throws IOException {
				
		
        Path sourceFile = templatesPath.resolve("simple_expected.md");
		TestUtilities.copyFromResource("simple_expected.md", sourceFile);
		
		try (Stream<String> lines = Files.lines(sourceFile)) {
			
			
			ValueMap valueMap = Stream.concat(lines, Stream.of("\n"))  // --> <String> Add a blank lines to the stream of lines so that all blocks are correctly terinates 
					                  .map(Block.block())              // --> <block> : Create block = [semantic-block] text-value | text-block | empty.
					                  .filter(b -> !b.isEmpty())       // --> <block> Filter out any empty blocks
					                  .map(b -> b.toValueMap())       // --> <valueMap> : Read the values and create a value map 
				                      .collect(ValueMap::new, ValueMap::merge, ValueMap::merge);  
			
			System.out.println(valueMap);
			
			assertEquals("Plato", valueMap.getValue("author").orElse(""));
			assertEquals("The Republic", valueMap.getValue("title").orElse(""));
			assertEquals("Benjamin Jowett", valueMap.getValue("translator").orElse(""));
			assertEquals("Wikisource", valueMap.getValue("source").orElse(""));
			assertEquals("https://en.wikisource.org/wiki/The_Republic", valueMap.getValue("sourceLink").orElse(""));
			assertEquals("4711", valueMap.getValue("id").orElse(""));
		}
 
	}
	

//	public static Function <String, Block> block() {
//		Block block = new Block(); 
//		return line -> { 
//			             if (line.isBlank()) { block.terminate(); return block;}  
//		                 //else if (line.contains("{{") && line.contains("}}")) {block.semantics(line); return block;}
//		                 else if (line.contains("{{") && line.contains("}}")) {block.initialise(line); return block;}
//                         else if (!block.isTerminated()) {block.appendText(line); return Block.empty();}
//                         else {return Block.empty();}  // Line has text that does not have an associated semantic block
//		               };
//	}
		
	
	public static Function <String, Optional<String>> chunk(){
		StringBuffer sb = new StringBuffer(80);
		return line -> { System.out.println("-----> state: " + sb);
			          if (line.isBlank()) { Optional<String> r = Optional.of(sb.toString()); sb.setLength(0); System.out.println("new"); return r;}
		              else {sb.append(line); return  Optional.empty();
		            }  
		              
		};

	}
	
		
}

