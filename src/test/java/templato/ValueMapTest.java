package templato;

//import java.util.Calendar;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import templato.valuemap.*;

class ValueMapTest {
	ValueMap valueMap;
	
	ValueMap[] testList = new ValueMap[6];
	
	@BeforeEach
	void setUp() throws Exception {
		valueMap = new ValueMap();
		
		for (int i = 0; i < testList.length; i++) {
	    	testList[i] = new ValueMap();
	    }
	    
	    // Julio-Claudian dynasty
	    testList[0].put("name", "Gaius");
		testList[0].put("familyName", "Augustus");
		testList[0].put("birthDate", LocalDate.of(-63, Month.SEPTEMBER, 23));
		
		testList[1].put("name", "Julius");
		testList[1].put("familyName", "Tiberius");
		testList[1].put("birthDate", LocalDate.of(-42, Month.NOVEMBER, 16));
	    
		testList[2].put("name", "Gaius");
		testList[2].put("familyName", "Caligula");
		testList[2].put("birthDate", LocalDate.of(12, Month.AUGUST, 31));
	    
	    
		// Nerva�Antonine dynasty
		testList[3].put("name", "Marcus");
		testList[3].put("familyName", "Nerva");
		testList[3].put("birthDate", LocalDate.of(30, Month.NOVEMBER, 8));
		
		testList[4].put("name", "Marcus");
		testList[4].put("familyName", "Aurelius");
		testList[4].put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		testList[5].put("name", "Lucius");
		testList[5].put("familyName", "Commodus");
		testList[5].put("birthDate", LocalDate.of(161, Month.AUGUST, 31));
		
	}


	@Test
	void testValues() {
		
		assertEquals("Augustus", testList[0].getValue("familyName").orElse(""));
		assertEquals("Gaius", testList[0].getValue("name").orElse(""));
		assertEquals(LocalDate.of(-63, Month.SEPTEMBER, 23), testList[0].getValue("birthDate").orElse(LocalDate.now()));
	
	}
	
	@Test
	void testValueMaps() {
		valueMap.put("title", "Roman Emperors");
		
		valueMap.put("emperor", testList[0]); 
		
		assertTrue(valueMap.containsField("emperor"));
		assertTrue(valueMap.isValueMap("emperor"));
		
		assertEquals(testList[0],valueMap.getValueMap("emperor").orElse(ValueMap.empty())); 
		
		
	}
	
	@Test
	void testLists() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].put("name", "Gaius Caesar");
		expectedMaps[0].put("familyName", "Aurelius");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		
		expectedMaps[1].put("name", "Marcus");
		expectedMaps[1].put("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].put("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].put("name", "Nero Claudius ");
		expectedMaps[2].put("familyName", "Caesar");  //Ditto
		expectedMaps[2].put("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
		
		
		for (ValueMap vm: expectedMaps) {
			valueMap.add("emperors", vm);
		}
		
		assertTrue(valueMap.containsField("emperors"));
		ValueMap emperorsVM = valueMap.getValueMap("emperors").orElse(valueMap.empty());
		assertTrue(emperorsVM.containsField("0"));
		assertTrue(emperorsVM.containsField("1"));
		assertTrue(emperorsVM.containsField("2"));
		
		
		Set<String> fieldNames = emperorsVM.fieldNames();
		assertEquals(Set.of("0", "1", "2"), fieldNames);
		
		List<ValueMap> actualMaps = new ArrayList<>();
		for (String fieldName: fieldNames) {
			actualMaps.add(emperorsVM.getValueMap(fieldName).orElse(ValueMap.empty()));
		}
		
		assertTrue(Arrays.deepEquals(expectedMaps, actualMaps.toArray()));
		
	}
	
	@Test
	void testListsUsingValueMaps () {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].put("name", "Gaius Caesar");
		expectedMaps[0].put("familyName", "Aurelius");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		
		expectedMaps[1].put("name", "Marcus");
		expectedMaps[1].put("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].put("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].put("name", "Nero Claudius ");
		expectedMaps[2].put("familyName", "Caesar");  //Ditto
		expectedMaps[2].put("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
	
		ValueMap emperors = new ValueMap();
		emperors.put("0", expectedMaps[0]);		
		emperors.put("1", expectedMaps[1]);		
		emperors.put("2", expectedMaps[2]);	  // TODO have a put with a int as key
		
		valueMap.put("emperors", emperors);
		
		assertTrue(valueMap.containsField("emperors"));
		ValueMap actualEmperors = valueMap.getValueMap("emperors").orElse(ValueMap.empty()); 
		
		List<ValueMap> actualMaps = new ArrayList<ValueMap>();
		actualMaps.add(actualEmperors.getValueMap("0").orElse(ValueMap.empty()));
		actualMaps.add(actualEmperors.getValueMap("1").orElse(ValueMap.empty()));
		actualMaps.add(actualEmperors.getValueMap("2").orElse(ValueMap.empty()));
		
					
		assertTrue(Arrays.deepEquals(expectedMaps, actualMaps.toArray()));
	}

	
	@Test
	void testMergeSimple() {
		// Set up a value map
		valueMap.put("name", "Marcus");
		valueMap.put("familyName", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		
	   // Setup another value map
		ValueMap anotherValueMap  = new ValueMap(); 
		anotherValueMap.put("name", "Marcus Aurelius Antoninus");
		anotherValueMap.put("reignStart", LocalDate.of(161, Month.MARCH, 8));
		anotherValueMap.put("reignEnd", LocalDate.of(180, Month.MARCH, 17));
		
		valueMap.merge(anotherValueMap);
		
		assertEquals("Marcus Aurelius Antoninus", valueMap.getValue("name").orElse(""));
		assertTrue(valueMap.containsField("reignStart"));
		assertEquals(LocalDate.of(161, Month.MARCH, 8), valueMap.getValue("reignStart").orElse(""));
		assertTrue(valueMap.containsField("reignEnd"));
		assertEquals(LocalDate.of(180, Month.MARCH, 17), valueMap.getValue("reignEnd").orElse(""));
		
	}
	
	@Test
	void testMergeComplex() {
		
		valueMap.put("source", "Wikipedia");
		valueMap.put("title", "Roman Emperors");
		
		ValueMap[] julioClaudianDynasty = new ValueMap[3];
		
		julioClaudianDynasty[0] = testList[0];
		julioClaudianDynasty[1] = testList[1];
		julioClaudianDynasty[2] = testList[2];
		
		valueMap.add("emperors", julioClaudianDynasty[0]);
		valueMap.add("emperors", julioClaudianDynasty[1]);
		valueMap.add("emperors", julioClaudianDynasty[2]);
		
		assertTrue(valueMap.containsField("emperors"));
		assertTrue(valueMap.isValueMap("emperors"));
		
        List<ValueMap> actualMaps = valueMap.getValueMaps("emperors");
		assertTrue(Arrays.deepEquals(julioClaudianDynasty, actualMaps.toArray()));
		
		ValueMap[] nervaAntonineDynasty = new ValueMap[3];
		nervaAntonineDynasty[0] = testList[3];
		nervaAntonineDynasty[1] = testList[4];
		nervaAntonineDynasty[2] = testList[5];
		
		
		ValueMap nervaAntonineDynastyListMap = new ValueMap();
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[0]);
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[1]);
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[2]);
		nervaAntonineDynastyListMap.put("comment", "Added Nerva-Antonine Dynasty");
			
		// Now add the new dynasty to the old value map
		valueMap.merge(nervaAntonineDynastyListMap);
		
        System.out.println(valueMap);
        
		actualMaps = valueMap.getValueMaps("emperors");
		assertTrue(Arrays.deepEquals(testList, actualMaps.toArray()));
		
		System.out.println(actualMaps);
		
	}
	
	@Test
	void testMergeWithNewList() {
		valueMap.put("source", "Wikipedia");
		valueMap.put("title", "Roman Emperors");
		
		ValueMap[] nervaAntonineDynasty = new ValueMap[3];
		nervaAntonineDynasty[0] = testList[3];
		nervaAntonineDynasty[1] = testList[4];
		nervaAntonineDynasty[2] = testList[5];
		
		
		ValueMap nervaAntonineDynastyListMap = new ValueMap();
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[0]);
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[1]);
		nervaAntonineDynastyListMap.add("emperors", nervaAntonineDynasty[2]);
		nervaAntonineDynastyListMap.put("comment", "Added Nerva-Antonine Dynasty");
			
		// Now add the new dynasty to the old value map
		valueMap.merge(nervaAntonineDynastyListMap);
			
		assertTrue(valueMap.containsField("emperors"));
		List<ValueMap> actualMaps = valueMap.getValueMaps("emperors");
		assertTrue(Arrays.deepEquals(nervaAntonineDynasty, actualMaps.toArray()));
		
	}
	
	@Test
	void testMergeWithExistingList() {
		valueMap.put("source", "Wikipedia");
		valueMap.put("title", "Roman Emperors");
		
		valueMap.add("emperors", testList[0]);
		valueMap.add("emperors", testList[1]);
		valueMap.add("emperors", testList[2]);
		
		ValueMap nervaAntonineDynastyListMap = new ValueMap();
		nervaAntonineDynastyListMap.add("emperors", testList[3]);
		nervaAntonineDynastyListMap.add("emperors", testList[4]);
		nervaAntonineDynastyListMap.add("emperors", testList[5]);
		nervaAntonineDynastyListMap.put("comment", "Added Nerva-Antonine Dynasty");
		
		valueMap.merge(nervaAntonineDynastyListMap);
		assertTrue(valueMap.containsField("emperors"));
		List<ValueMap> actualMaps = valueMap.getValueMaps("emperors");
		assertTrue(Arrays.deepEquals(testList, actualMaps.toArray()));
		
		
	}
	


	@Test
	void testOrdinalValueMap() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());
		
		expectedMaps[0].put("name", "Gaius Caesar");
		expectedMaps[0].put("familyName", "Aurelius");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		expectedMaps[1].put("name", "Marcus");
		expectedMaps[1].put("familyName", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].put("birthDate", LocalDate.of(-100, Month.JULY, 12));
		
		expectedMaps[2].put("name", "Nero Claudius ");
		expectedMaps[2].put("familyName", "Caesar");  //Ditto
		expectedMaps[2].put("birthDate", LocalDate.of(37, Month.DECEMBER, 15));
		
		for (ValueMap vm: expectedMaps) {
			valueMap.add("emperors", vm);
		}
		valueMap.put("title", "Roman Emperors");
		
		assertTrue(valueMap.isValueMap("emperors"));
		assertFalse(valueMap.isValueMap("title"));
		
		
	}

	@Test
	void testContainsField() {
		valueMap.put("name", "Marcus");
		valueMap.put("familyName", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		assertTrue(valueMap.containsField("name"));
		assertTrue(valueMap.containsField("birthDate"));
		assertFalse(valueMap.containsField("somethingElse"));
		
		
	}
	


}
