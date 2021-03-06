package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class TemplateTestAllTypes {
final static String templateFileName = "all_types_template.md";
	
	private static FileSystem fileSystem;
	private Path rootPath;
	private Path templatesPath;
	
	private Path templateFile;
	
	// Test data object
	AllTypes allTypes;;

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
		
		
		// Set up the all types object. This has fields for each supported type.
		allTypes = new AllTypes();

		allTypes.setStr("test string");
		allTypes.setIntWrapper(Integer.valueOf(42));  
		allTypes.setIntPrimitive(42);
		allTypes.setShortWrapper(Short.valueOf((short)2));  
		allTypes.setShortPrimitive((short)22);
		allTypes.setByteWrapper(Byte.valueOf((byte)0x3));  
		allTypes.setBytePrimitive((byte)0x3);
		allTypes.setLongWrapper(Long.valueOf(123_456_789L));  
		allTypes.setLongPrimitive(123_456_789L);
		allTypes.setDoubleWrapper(Double.valueOf(2.99_792_458e8));  
		allTypes.setDoublePrimitive(2.99_792_458e8);
		allTypes.setFloatWrapper(Float.valueOf(3.141592654F));  
		allTypes.setFloatPrimitive(3.141592654F);
		allTypes.setBooleanWrapper(Boolean.valueOf("true"));  
		allTypes.setBooleanPrimitive(true);
		allTypes.setCharacterWrapper(Character.valueOf('c'));  
		allTypes.setCharacterPrimitive('c');
		allTypes.setLocalDate(LocalDate.of(1956, 1, 9));
		allTypes.setLocalDateTime(LocalDateTime.of(1956, 1, 9, 20, 06));
		allTypes.setZonedDateTime(ZonedDateTime.of(LocalDateTime.of(1956, 1, 9, 20, 06), ZoneId.of("UTC+00:00")));
		allTypes.setURL(new URL("https://github.com/adoble/semplate"));
				
	}

	@AfterEach
	void tearDown() throws Exception {
		fileSystem.close();
	}

	/** Generate and read all supported basic types (excluding lists)
	 * 
	 * These are: 
	 *   - String
	 *   - Integer and int
	 *   - Short and short
	 *   - Byte and byte
	 *   - Long and long
	 *   - Double and double
	 *   - Float and float
	 *   - Boolean and boolean
	 *   - Character and char
	 *   - LocalDate
	 *   - LocalDateTime
	 *   - ZonedDateTime
	 *   - URL
	 *   
	 *   Uses the class AllTypes to test this. 
	 */
	
	@Test
	void testAllTypes() throws Exception {

		// Generate a file from the using the data in the allTypes object
		//Path outputPath = templatesPath.resolve("all_types_test.md");
		Path outputPath = rootPath.resolve("all_types_test.md");

		SemanticWriter.with(allTypes).usingTemplate(templateFile).write(outputPath);
		
		// Now read in the file and see if the object has the same values
		//AllTypes allTypesRead = (AllTypes) template.read(AllTypes.class, outputPath);
		
		AllTypes allTypesRead = (AllTypes) SemanticReader.with(AllTypes.class).usingFile(outputPath).read(); 
		assertNotNull(allTypesRead);
		
		assertEquals(allTypesRead.getStr(), "test string");
		assertEquals(allTypesRead.getIntWrapper(), Integer.valueOf(42));  
		assertEquals(allTypesRead.getIntPrimitive(), 42);
		assertEquals(allTypesRead.getShortWrapper(), Short.valueOf((short)2));  
		assertEquals(allTypesRead.getShortPrimitive(), (short)22);
		assertEquals(allTypesRead.getByteWrapper(), Byte.valueOf((byte)0x3));  
		assertEquals(allTypesRead.getBytePrimitive(), (byte)0x3);
		assertEquals(allTypesRead.getLongWrapper(),Long.valueOf(123_456_789L));  
		assertEquals(allTypesRead.getLongPrimitive(), 123_456_789L);
		assertEquals(allTypesRead.getDoubleWrapper(), Double.valueOf(2.99_792_458e8));  
		assertEquals(allTypesRead.getDoublePrimitive(), 2.99_792_458e8);
		assertEquals(allTypesRead.getFloatWrapper(), Float.valueOf(3.141592654F));  
		assertEquals(allTypesRead.getFloatPrimitive(), 3.141592654F);
		assertEquals(allTypesRead.getBooleanWrapper(), Boolean.valueOf("true"));  
		assertEquals(allTypesRead.getBooleanPrimitive(), true);
		assertEquals(allTypesRead.getCharacterWrapper(), Character.valueOf('c'));  
		assertEquals(allTypesRead.getCharacterPrimitive(), 'c');
		assertEquals(allTypesRead.getLocalDate(), LocalDate.of(1956, 1, 9));
		assertEquals(allTypesRead.getLocalDateTime(), LocalDateTime.of(1956, 1, 9, 20, 06));
		assertEquals(allTypesRead.getZonedDateTime(), ZonedDateTime.of(LocalDateTime.of(1956, 1, 9, 20, 06), ZoneId.of("UTC+00:00")));
		assertEquals(allTypesRead.getURL(), new URL("https://github.com/adoble/semplate"));
		
	}
	
}
