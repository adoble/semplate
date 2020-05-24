package templato;

//import java.util.Calendar;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
	    
	    
		// Nerva–Antonine dynasty
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
	void testDataObjects() {
		valueMap.put("name", "Marcus");
		valueMap.put("familyName", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));
		
		

		assertEquals("Aurelius", valueMap.getValue("familyName"));
		assertEquals("Marcus", valueMap.getValue("name"));
		assertEquals(LocalDate.of(121, Month.APRIL, 26), valueMap.getValue("birthDate"));
	
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
		assertTrue(valueMap.isList("emperors"));
		List<ValueMap> actualMaps = valueMap.getList("emperors");
		
		
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
		ValueMap actualEmperors = (ValueMap) valueMap.getValue("emperors");  // TODO new function getValueMap() that just returns valuemaps
		List<ValueMap> actualMaps = new ArrayList<ValueMap>();
		actualMaps.add((ValueMap)actualEmperors.getValue("0"));
		actualMaps.add((ValueMap)actualEmperors.getValue("1"));
		actualMaps.add((ValueMap)actualEmperors.getValue("2"));
		
					
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
		
		assertEquals("Marcus Aurelius Antoninus", valueMap.getValue("name"));
		assertTrue(valueMap.containsField("reignStart"));
		assertEquals(LocalDate.of(161, Month.MARCH, 8), valueMap.getValue("reignStart"));
		assertTrue(valueMap.containsField("reignEnd"));
		assertEquals(LocalDate.of(180, Month.MARCH, 17), valueMap.getValue("reignEnd"));
		
	}
	
	@Test
	void testMergeWithList() {
		
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
		assertTrue(valueMap.isList("emperors"));
		
        List<ValueMap> actualMaps = valueMap.getList("emperors");
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
		
	
		actualMaps = valueMap.getList("emperors");
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
		List<ValueMap> actualMaps = valueMap.getList("emperors");
		assertTrue(Arrays.deepEquals(nervaAntonineDynasty, actualMaps.toArray()));
		
	}
	
	@Test
	void testMergeWithLotsOfSmallValueMaps() {
		ValueMap starTrek1 = new ValueMap(); 
		starTrek1.put("title", "Star Trek");
		
		ValueMap starTrek2 = new ValueMap();
		starTrek2.put("producer", "Gene Roddenberry");
		
		ValueMap character1FamilyName = new ValueMap();
		character1FamilyName.put("familyName", "Kirk");
		
		ValueMap character1Name = new ValueMap(); 
		character1Name.put("name", "James");
		
		ValueMap character2FamilyName = new ValueMap();
		character2FamilyName.put("familyName", "McCoy");
		
		ValueMap character2Name = new ValueMap(); 
		character2Name.put("name", "Leonard");
		
		ValueMap character3Name = new ValueMap();
		character3Name.put("name", "Spock");
		
		ValueMap character1 = new ValueMap();
		character1.merge(character1FamilyName);
		character1.merge(character1Name);
		
		ValueMap character2 = new ValueMap();
		character2.merge(character2FamilyName);
		character2.merge(character2Name);
		
		ValueMap character3 = new ValueMap();
		character3.merge(character3Name);
		
		
		ValueMap characters = new ValueMap(); 
		characters.put("1", character1);
		characters.put("2", character2);
		characters.put("3", character3);
		
		ValueMap characterList = new ValueMap();
		characterList.put("characters", characters);
		
		
		// Now merge- TODO return merged value map so these can be chained.
		
		valueMap.merge(starTrek1); 
		valueMap.merge(starTrek2);
		valueMap.merge(characterList);
		
		System.out.println("testMergeWithLotsOfSmallValueMaps\n" + valueMap);
		
		assertTrue(valueMap.containsField("producer"));
		assertEquals("Gene Roddenberry", (String) valueMap.getValue("producer"));
		
		assertEquals("Leonard", valueMap.getValueMap("characters").getValueMap("2").getValue("name"));
		
		
		
	}

	@Test
	void testIsList() {
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
		
		assertTrue(valueMap.isList("emperors"));
		assertFalse(valueMap.isList("title"));
		
		
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
