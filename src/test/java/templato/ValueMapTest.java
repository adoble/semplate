package templato;

//import java.util.Calendar;
import java.time.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueMapTest {
	ValueMap valueMap;
	
	@BeforeEach
	void setUp() throws Exception {
	  valueMap = new ValueMap();
	}
	

	@Test
	void testDataObjects() {
		valueMap.putObject("name", "Marcus");
		valueMap.putObject("familyName", "Aurelius");
		valueMap.putObject("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		

		assertEquals("Aurelius", valueMap.getDataObject("familyName"));
		assertEquals("Marcus", valueMap.getDataObject("name"));
		assertEquals(LocalDate.of(121, Month.APRIL, 26), valueMap.getDataObject("birthDate"));
	
	}

	@Test
	void testLists() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].putObject("name", "Gaius Caesar");
		expectedMaps[0].putObject("familyName", "Aurelius");
		expectedMaps[0].putObject("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		expectedMaps[1].putObject("name", "Marcus");
		expectedMaps[1].putObject("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].putObject("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].putObject("name", "Nero Claudius ");
		expectedMaps[2].putObject("familyName", "Caesar");  //Ditto
		expectedMaps[2].putObject("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
		
		valueMap.putList("emperors", Arrays.asList(expectedMaps));
		
		List<ValueMap> actualMaps = valueMap.getDataList("emperors");
		
		
		assertTrue(Arrays.deepEquals(expectedMaps, actualMaps.toArray()));
		
	}

	
	@Test
	void testPutAllSimple() {
		// Set up a value map
		valueMap.putObject("name", "Marcus");
		valueMap.putObject("familyName", "Aurelius");
		valueMap.putObject("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		
	   // Setup another value map
		ValueMap anotherValueMap  = new ValueMap(); 
		anotherValueMap.putObject("name", "Marcus Aurelius Antoninus");
		anotherValueMap.putObject("reignStart", LocalDate.of(161, Month.MARCH, 8));
		anotherValueMap.putObject("reignEnd", LocalDate.of(180, Month.MARCH, 17));
		
		valueMap.putAll(anotherValueMap);
		
		assertEquals("Marcus Aurelius Antoninus", valueMap.getDataObject("name"));
		assertTrue(valueMap.containsField("reignStart"));
		assertEquals(LocalDate.of(161, Month.MARCH, 8), valueMap.getDataObject("reignStart"));
		assertTrue(valueMap.containsField("reignEnd"));
		assertEquals(LocalDate.of(180, Month.MARCH, 17), valueMap.getDataObject("reignEnd"));
		
	}

	@Test
	void testIsList() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].putObject("name", "Gaius Caesar");
		expectedMaps[0].putObject("familyName", "Aurelius");
		expectedMaps[0].putObject("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		expectedMaps[1].putObject("name", "Marcus");
		expectedMaps[1].putObject("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].putObject("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].putObject("name", "Nero Claudius ");
		expectedMaps[2].putObject("familyName", "Caesar");  //Ditto
		expectedMaps[2].putObject("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
        valueMap.putList("emperors", Arrays.asList(expectedMaps));
        valueMap.putObject("title", "Roman Emperors");
		
		assertTrue(valueMap.isList("emperors"));
		assertFalse(valueMap.isList("title"));
		
		
	}

	@Test
	void testContainsField() {
		valueMap.putObject("name", "Marcus");
		valueMap.putObject("familyName", "Aurelius");
		valueMap.putObject("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		assertTrue(valueMap.containsField("name"));
		assertTrue(valueMap.containsField("birthDate"));
		assertFalse(valueMap.containsField("somethingElse"));
		
		
	}

}
