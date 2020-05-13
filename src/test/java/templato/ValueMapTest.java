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
		valueMap.putObjectValue("name", "Marcus");
		valueMap.putObjectValue("familyName", "Aurelius");
		valueMap.putObjectValue("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		

		assertEquals("Aurelius", valueMap.getDataValue("familyName"));
		assertEquals("Marcus", valueMap.getDataValue("name"));
		assertEquals(LocalDate.of(121, Month.APRIL, 26), valueMap.getDataValue("birthDate"));
	
	}

	@Test
	void testLists() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].putObjectValue("name", "Gaius Caesar");
		expectedMaps[0].putObjectValue("familyName", "Aurelius");
		expectedMaps[0].putObjectValue("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		expectedMaps[1].putObjectValue("name", "Marcus");
		expectedMaps[1].putObjectValue("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].putObjectValue("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].putObjectValue("name", "Nero Claudius ");
		expectedMaps[2].putObjectValue("familyName", "Caesar");  //Ditto
		expectedMaps[2].putObjectValue("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
		
		valueMap.putListValue("emperors", Arrays.asList(expectedMaps));
		
		List<ValueMap> actualMaps = valueMap.getListValue("emperors");
		
		
		assertTrue(Arrays.deepEquals(expectedMaps, actualMaps.toArray()));
		
	}

	
	@Test
	void testPutAllSimple() {
		// Set up a value map
		valueMap.putObjectValue("name", "Marcus");
		valueMap.putObjectValue("familyName", "Aurelius");
		valueMap.putObjectValue("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		
	   // Setup another value map
		ValueMap anotherValueMap  = new ValueMap(); 
		anotherValueMap.putObjectValue("name", "Marcus Aurelius Antoninus");
		anotherValueMap.putObjectValue("reignStart", LocalDate.of(161, Month.MARCH, 8));
		anotherValueMap.putObjectValue("reignEnd", LocalDate.of(180, Month.MARCH, 17));
		
		valueMap.putAll(anotherValueMap);
		
		assertEquals("Marcus Aurelius Antoninus", valueMap.getDataValue("name"));
		assertTrue(valueMap.containsField("reignStart"));
		assertEquals(LocalDate.of(161, Month.MARCH, 8), valueMap.getDataValue("reignStart"));
		assertTrue(valueMap.containsField("reignEnd"));
		assertEquals(LocalDate.of(180, Month.MARCH, 17), valueMap.getDataValue("reignEnd"));
		
	}

	@Test
	void testIsList() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].putObjectValue("name", "Gaius Caesar");
		expectedMaps[0].putObjectValue("familyName", "Aurelius");
		expectedMaps[0].putObjectValue("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		expectedMaps[1].putObjectValue("name", "Marcus");
		expectedMaps[1].putObjectValue("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].putObjectValue("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].putObjectValue("name", "Nero Claudius ");
		expectedMaps[2].putObjectValue("familyName", "Caesar");  //Ditto
		expectedMaps[2].putObjectValue("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
        valueMap.putListValue("emperors", Arrays.asList(expectedMaps));
        valueMap.putObjectValue("title", "Roman Emperors");
		
		assertTrue(valueMap.isList("emperors"));
		assertFalse(valueMap.isList("title"));
		
		
	}

	@Test
	void testContainsField() {
		valueMap.putObjectValue("name", "Marcus");
		valueMap.putObjectValue("familyName", "Aurelius");
		valueMap.putObjectValue("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		assertTrue(valueMap.containsField("name"));
		assertTrue(valueMap.containsField("birthDate"));
		assertFalse(valueMap.containsField("somethingElse"));
		
		
	}

}
