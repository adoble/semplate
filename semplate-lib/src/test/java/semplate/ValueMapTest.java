package semplate;

import java.net.URL;
//import java.util.Calendar;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import semplate.valuemap.*;

class ValueMapTest {
	ValueMap valueMap;

	ValueMap[] maps = new ValueMap[6];

	@BeforeEach
	void setUp() throws Exception {
		valueMap = new ValueMap();

		for (int i = 0; i < maps.length; i++) {
	    	maps[i] = new ValueMap();
	    }

	    // Julio-Claudian dynasty
	    maps[0].put("praenomen", "Gaius");
		maps[0].put("nomen", "Augustus");
		maps[0].put("birthDate", LocalDate.of(-63, Month.SEPTEMBER, 23));

		maps[1].put("praenomen", "Julius");
		maps[1].put("nomen", "Tiberius");
		maps[1].put("birthDate", LocalDate.of(-42, Month.NOVEMBER, 16));

		maps[2].put("praenomen", "Gaius");
		maps[2].put("nomen", "Caligula");
		maps[2].put("birthDate", LocalDate.of(12, Month.AUGUST, 31));


		// Nerva-Antonine dynasty
		maps[3].put("praenomen", "Marcus");
		maps[3].put("nomen", "Nerva");
		maps[3].put("birthDate", LocalDate.of(30, Month.NOVEMBER, 8));

		maps[4].put("praenomen", "Marcus");
		maps[4].put("nomen", "Aurelius");
		maps[4].put("birthDate", LocalDate.of(121, Month.APRIL, 26));

		maps[5].put("praenomen", "Lucius");
		maps[5].put("nomen", "Commodus");
		maps[5].put("birthDate", LocalDate.of(161, Month.AUGUST, 31));

	}


	@Test
	void testValues() {

		assertEquals("Augustus", maps[0].getValue("nomen").orElse(""));
		assertEquals("Gaius", maps[0].getValue("praenomen").orElse(""));
		assertEquals(LocalDate.of(-63, Month.SEPTEMBER, 23), maps[0].getValue("birthDate").orElse(LocalDate.now()));

		valueMap.put("empty", null);
		assertEquals(Optional.empty(), valueMap.getValue("empty"));

		ValueMap consulVM = new ValueMap();
		consulVM.put("praenomen", "Lucius");
		consulVM.put("nomen", "Junius");
		consulVM.put("cognomen", "Brutus");
		valueMap.put("consul", consulVM);
		assertEquals(Optional.empty(), valueMap.getValue("consul"));



	}


	@Test
	void testValueMaps() {
		valueMap.put("title", "Roman Emperors");

		valueMap.put("emperor", maps[0]);

		assertTrue(valueMap.containsField("emperor"));
		assertTrue(valueMap.isValueMap("emperor"));

		assertEquals(maps[0],valueMap.getValueMap("emperor").orElse(ValueMap.empty()));

		assertEquals(Optional.empty(), valueMap.getValue("emperor"));


		valueMap.put("null_value", null);
		assertEquals(Optional.empty(), valueMap.getValueMap("null_value"));

		assertEquals(Optional.empty(), valueMap.getValueMap("title"));


	}

	@Test
	void testDotNotationPut() {
		valueMap.put("emperors.0.praenomen", "Marcus");

		assertTrue(valueMap.containsField("emperors"));
		ValueMap emperorsVM = valueMap.getValueMap("emperors").orElse(ValueMap.empty());
		assertFalse(emperorsVM.isEmpty());

		assertTrue(emperorsVM.containsField("0"));
		ValueMap emperorVM = emperorsVM.getValueMap("0").orElse(ValueMap.empty());
		assertFalse(emperorsVM.isEmpty());

		assertTrue(emperorVM.containsField("praenomen"));
		assertEquals("Marcus", emperorVM.getValue("praenomen").orElse(""));


	}
	
	@Test 
	void testDotNotationManyPuts() {
		valueMap.put("emperors.0.praenomen", "Marcus");
		valueMap.put("emperors.1.praenomen", "Claudius");
		
		assertEquals("Marcus", valueMap.getValue("emperors.0.praenomen").orElse(""));
		assertEquals("Claudius", valueMap.getValue("emperors.1.praenomen").orElse(""));
		
		valueMap.put("emperors.2.praenomen", "Gaius");
		assertEquals("Gaius", valueMap.getValue("emperors.2.praenomen").orElse(""));
			
	}

	@Test
	void testDotNotationGetValue1() {
		valueMap.put("emperors.0.praenomen", "Marcus");

		Optional<Object> value = valueMap.getValue("emperors.0.praenomen");
		assertNotNull(value);
		assertTrue(value.isPresent());
		assertEquals("Marcus", value.orElse("").toString());

	}

	@Test
	void testDotNotationGetValue2() {
		ValueMap emperors = new ValueMap();

		emperors.put("emperors.0", maps[0]);
		emperors.put("emperors.1", maps[1]);
		emperors.put("emperors.2", maps[2]);


		Optional<Object> value = emperors.getValue("emperors.1.praenomen");
		assertNotNull(value);
		assertTrue(value.isPresent());
		assertEquals("Julius", value.orElse("").toString());
	}

	@Test
	void testDotNotationGetValueMap() {
		ValueMap emperors = new ValueMap();

		emperors.put("emperors.0", maps[0]);
		emperors.put("emperors.1", maps[1]);
		emperors.put("emperors.2", maps[2]);


		Optional<ValueMap> vm = emperors.getValueMap("emperors.1");
		assertNotNull(vm);
		assertFalse(vm.isEmpty());
		assertEquals(Optional.of("Julius"), vm.flatMap(v -> v.getValue("praenomen")));
	}

	@Test
	void testDotNotationError() {
		valueMap.put("emperors.2.praenomen", "Marcus");

		Optional<Object> value1 = valueMap.getValue("xxx.0.praenomen");  // Does not exist!
		assertEquals(Optional.empty(), value1);

		Optional<Object> value2 = valueMap.getValue("emperors.0.praenomen");  // Does not exist!
		assertEquals(Optional.empty(), value2);

		Optional<Object> value3 = valueMap.getValue("emperors.2.xxx");  // Does not exist!
		assertEquals(Optional.empty(), value3);


	}
	
	@Test
	void testAdd() {
		final int nMaps = 6;
		
		ValueMap[] maps = new ValueMap[nMaps];
		for (int i = 0; i < nMaps ; i++) maps[i] = new ValueMap();

		maps[0].put("nomen", "Aurelius");

		maps[1].put("nomen", "Septimius");

		maps[2].put("nomen", "Claudius");

		ValueMap expectedMap = new ValueMap();
		expectedMap.add("emperors", "0", maps[0]);

		assertEquals("Aurelius", expectedMap.getValue("emperors.0.nomen").orElse(""));


		expectedMap.add("emperors", "1", maps[1]);
		expectedMap.add("emperors", "2", maps[2]);

		assertEquals("(emperors=(0=(nomen=Aurelius),1=(nomen=Septimius),2=(nomen=Claudius)))",
				     expectedMap.toString());
		
		
		maps[3].put("nomen", "Nerva");
		maps[4].put("nomen", "Aurelius");
		maps[5].put("nomen", "Commodus");
		
		expectedMap.add("emperors", "3", maps[3])
		           .add("emperors", "4", maps[4])
		           .add("emperors", "5", maps[5]);
		
		List<ValueMap> allLists = expectedMap.getValueMaps("emperors");
		assertEquals(6, allLists.size());
		
		assertEquals("Commodus", expectedMap.getValue("emperors.5.nomen").orElse(""));
		assertEquals("Aurelius", expectedMap.getValue("emperors.4.nomen").orElse(""));
		assertEquals("Nerva",    expectedMap.getValue("emperors.3.nomen").orElse(""));
		
	}
	
	@Test
	void testAddWithOrdinals() {
      ValueMap expectedMap = ValueMap.empty();
      
      expectedMap.put("emperors.0", maps[0]);
      expectedMap.put("emperors.1", maps[1]);
      expectedMap.put("emperors.2", maps[2]);
      
      assertEquals("Augustus", expectedMap.getValue("emperors.0.nomen").orElse(""));
      assertEquals("Tiberius", expectedMap.getValue("emperors.1.nomen").orElse(""));
      assertEquals("Caligula", expectedMap.getValue("emperors.2.nomen").orElse(""));
	
	}
	
	@Test
	void testAddWithOrdinalsAndStart() {
		ValueMap expectedMap = ValueMap.empty();

		expectedMap.put("emperors.6", maps[0]);
		expectedMap.put("emperors.7", maps[1]);
		expectedMap.put("emperors.8", maps[2]);

		assertEquals("Augustus", expectedMap.getValue("emperors.6.nomen").orElse(""));
		assertEquals("Tiberius", expectedMap.getValue("emperors.7.nomen").orElse(""));
		assertEquals("Caligula", expectedMap.getValue("emperors.8.nomen").orElse(""));

	}
	
	@Test
	void testGetValueMaps() {

		ValueMap testMap = ValueMap.empty();

		testMap.put("emperors.0", maps[0]);
		testMap.put("emperors.1", maps[1]);
		testMap.put("emperors.2", maps[2]);
		
	    ValueMap vm = testMap.getValueMap("emperors").orElse(ValueMap.empty());
	    List<ValueMap> vmList = vm.getValueMaps();
	    
	    assertEquals("Augustus", vmList.get(0).getValue("nomen").orElse(""));
	    assertEquals("Tiberius", vmList.get(1).getValue("nomen").orElse(""));
	    assertEquals("Caligula", vmList.get(2).getValue("nomen").orElse(""));
		
	
	}
	
	@Test
	void testGetValueMapsWithFieldName() {
		
		ValueMap testMap = ValueMap.empty();

		testMap.put("emperors.0", maps[0]);
		testMap.put("emperors.1", maps[1]);
		testMap.put("emperors.2", maps[2]);
		
	    List<ValueMap> vm = testMap.getValueMaps("emperors");
	    
	    
	    assertEquals("Augustus", vm.get(0).getValue("nomen").orElse(""));
	    assertEquals("Tiberius", vm.get(1).getValue("nomen").orElse(""));
	    assertEquals("Caligula", vm.get(2).getValue("nomen").orElse(""));
		
	}
	

	@Test
	void testLists() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());

		expectedMaps[0].put("praenomen", "Marcus");
		expectedMaps[0].put("nomen", "Aurelius");
		expectedMaps[0].put("cognomen", "Antoninus Augustus");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));


		expectedMaps[1].put("praenomen", "Lucius");
		expectedMaps[1].put("nomen", "Septimius");
		expectedMaps[1].put("cognomen", "Septimius");
		expectedMaps[1].put("birthDate", LocalDate.of(189, Month.MARCH, 7));

		expectedMaps[2].put("praenomen", "Nero ");
		expectedMaps[2].put("nom", "Claudius");
		expectedMaps[2].put("cognom", "Augustus Germanicus");
		expectedMaps[2].put("birthDate", LocalDate.of(37, Month.DECEMBER, 15));



		int i = 0;
		for (ValueMap vm: expectedMaps) {
			valueMap.put("emperors" + "." + i++, vm);
		}

		assertTrue(valueMap.containsField("emperors"));
		ValueMap emperorsVM = valueMap.getValueMap("emperors").orElse(ValueMap.empty());
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

		expectedMaps[0].put("praenomen", "Marcus");
		expectedMaps[0].put("nomen", "Aurelius");
		expectedMaps[0].put("cognomen", "Antoninus Augustus");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));


		expectedMaps[1].put("praenomen", "Titus");
		expectedMaps[1].put("nomen", "Flavius");
		expectedMaps[1].put("cognomen", "Vespasianus");
		expectedMaps[1].put("birthDate", LocalDate.of(9, Month.NOVEMBER, 17));

		expectedMaps[2].put("praenomen", "Nero");
		expectedMaps[2].put("nomen", "Nero");
		expectedMaps[2].put("cognomen", "Caesar");  //Ditto
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

		julioClaudianDynasty[0] = maps[0];
		julioClaudianDynasty[1] = maps[1];
		julioClaudianDynasty[2] = maps[2];

//		valueMap.add("emperors", julioClaudianDynasty[0]);
//		valueMap.add("emperors", julioClaudianDynasty[1]);
//		valueMap.add("emperors", julioClaudianDynasty[2]);
		
		valueMap.add("emperors", "0", julioClaudianDynasty[0]);
		valueMap.add("emperors", "1", julioClaudianDynasty[1]);
		valueMap.add("emperors", "2", julioClaudianDynasty[2]);

		assertTrue(valueMap.containsField("emperors"));
		assertTrue(valueMap.isValueMap("emperors"));

        List<ValueMap> actualMaps = valueMap.getValueMaps("emperors");
		assertTrue(Arrays.deepEquals(julioClaudianDynasty, actualMaps.toArray()));

		ValueMap[] nervaAntonineDynasty = new ValueMap[3];
		nervaAntonineDynasty[0] = maps[3];
		nervaAntonineDynasty[1] = maps[4];
		nervaAntonineDynasty[2] = maps[5];


		ValueMap nervaAntonineDynastyListMap = new ValueMap();
		nervaAntonineDynastyListMap.add("emperors", "3", nervaAntonineDynasty[0]);
		nervaAntonineDynastyListMap.add("emperors", "4", nervaAntonineDynasty[1]);
		nervaAntonineDynastyListMap.add("emperors", "5", nervaAntonineDynasty[2]);
		nervaAntonineDynastyListMap.put("comment", "Added Nerva-Antonine Dynasty");

		// Now add the new dynasty to the old value map
		valueMap.merge(nervaAntonineDynastyListMap);

        actualMaps = valueMap.getValueMaps("emperors");
        
        List<ValueMap> expectedMaps = Lists.newArrayList(maps).subList(0,  6);
		
        assertEquals(expectedMaps, actualMaps);
        //assertTrue(Arrays.deepEquals(testList, actualMaps.toArray()));

	}

	
	@Test
	void testMergeWithNewList() {
		valueMap.put("source", "Wikipedia");
		valueMap.put("title", "Roman Emperors");

		ValueMap[] nervaAntonineDynasty = new ValueMap[3];
		nervaAntonineDynasty[0] = maps[3];
		nervaAntonineDynasty[1] = maps[4];
		nervaAntonineDynasty[2] = maps[5];


		ValueMap nervaAntonineDynastyListMap = new ValueMap();

		nervaAntonineDynastyListMap.put("emperors.0", nervaAntonineDynasty[0]);
		nervaAntonineDynastyListMap.put("emperors.1", nervaAntonineDynasty[1]);
		nervaAntonineDynastyListMap.put("emperors.2", nervaAntonineDynasty[2]);
		
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

		valueMap.put("emperors.0", maps[0]);
		valueMap.put("emperors.1", maps[1]);
		valueMap.put("emperors.2", maps[2]);

		ValueMap nervaAntonineDynastyListMap = new ValueMap();

		nervaAntonineDynastyListMap.put("emperors.0", maps[3]);
		nervaAntonineDynastyListMap.put("emperors.1", maps[4]);
		nervaAntonineDynastyListMap.put("emperors.2", maps[5]);

		nervaAntonineDynastyListMap.put("comment", "Added Nerva-Antonine Dynasty");

		valueMap.merge(nervaAntonineDynastyListMap);
		assertTrue(valueMap.containsField("emperors"));
		List<ValueMap> actualMaps = valueMap.getValueMaps("emperors");

		List<ValueMap> expectedMaps = Lists.newArrayList(maps).subList(0, 3);

		assertEquals(expectedMaps, actualMaps);


	}



	@Test
	void testOrdinalValueMap() {
		ValueMap[] expectedMaps = new ValueMap[3];
	    Arrays.fill(expectedMaps, new ValueMap());

		expectedMaps[0].put("praenomen", "Gaius");
		expectedMaps[0].put("nomen", "Aurelius");
		expectedMaps[0].put("birthDate", LocalDate.of(121, Month.APRIL, 26));

		expectedMaps[1].put("praenomen", "Marcus");
		expectedMaps[1].put("nomen", "Aurelius");  // Not historically correct, but it's only a test
		expectedMaps[1].put("birthDate", LocalDate.of(-100, Month.JULY, 12));

		expectedMaps[2].put("praenomen", "Nero");
		expectedMaps[2].put("nomen", "Claudius ");  //Ditto
		expectedMaps[2].put("birthDate", LocalDate.of(37, Month.DECEMBER, 15));

		int i = 0;
		for (ValueMap vm: expectedMaps) {
			valueMap.put("emperors." + i, vm);
		}
		valueMap.put("title", "Roman Emperors");

		assertTrue(valueMap.isValueMap("emperors"));
		assertFalse(valueMap.isValueMap("title"));


	}

	@Test
	void testContainsField() {
		valueMap.put("praenomen", "Marcus");
		valueMap.put("nomen", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));

		assertTrue(valueMap.containsField("nomen"));
		assertTrue(valueMap.containsField("birthDate"));
		assertFalse(valueMap.containsField("somethingElse"));


	}

	@Test
	void testMerge2() {


		valueMap.put("praenomen", "Marcus");
		valueMap.put("nomen", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));

		ValueMap  vmToMerge = new ValueMap();
		vmToMerge.put("praenomen", "Mark");
		vmToMerge.put("nomen", "Ori");
		vmToMerge.put("birthDate", LocalDate.of(2021, Month.APRIL, 26));

		valueMap.merge(vmToMerge);

		assertEquals("(praenomen=Mark,nomen=Ori,birthDate=2021-04-26)", valueMap.toString());

	}

	@Test
	void testMerge3() {

		valueMap.put("praenomen", "Marcus");
		valueMap.put("nomen", "Aurelius");
		valueMap.put("birthDate", LocalDate.of(121, Month.APRIL, 26));

		ValueMap child1 = (new ValueMap()).put("praenomen", "Pricilla").put("nomen", "Aurelius");
		ValueMap toMerge = (new ValueMap()).put("children.0", child1);
		valueMap.merge(toMerge);


		toMerge = (new ValueMap()).put("children.1",  (new ValueMap().put("praenomen", "Octavia").put("nomen", "Aurelius")));
		valueMap.merge(toMerge);


		valueMap.merge((new ValueMap()).put("children.2",  (new ValueMap().put("praenomen", "Tatiana").put("nomen", "Aurelius"))));

		assertEquals("(praenomen=Marcus,"
				+ "children=(0=(praenomen=Pricilla,nomen=Aurelius),"
				+ "1=(praenomen=Octavia,nomen=Aurelius),"
				+ "2=(praenomen=Tatiana,nomen=Aurelius)),"
				+ "nomen=Aurelius,birthDate=0121-04-26)",
				valueMap.toString());
	}



	@Test
	void testEmpty() {
		ValueMap vm = new ValueMap();
		assertTrue(vm.isEmpty());

		vm = ValueMap.empty();
		assertTrue(vm.isEmpty());

		vm.put("some_fieldname", "some_value");
		assertFalse(vm.isEmpty());


	}

	@Test
	void testToString() {
		String expected = "(comment=Not complete,"
				+ "emperors=("
				+ "0=(praenomen=Gaius,nomen=Augustus,birthDate=-0063-09-23),"
				+ "1=(praenomen=Julius,nomen=Tiberius,birthDate=-0042-11-16)"
				+ "),"
				+ "title=Roman Emperors)";

		valueMap.put("title", "Roman Emperors");
		valueMap.put("comment", "Not complete");
		for (int i = 0; i < 2; i++) {
			valueMap.put("emperors." + i, maps[i]);
		}

		assertEquals(expected, valueMap.toString());

	}
	

	
	@Test
	void testCreationWithString() {
		ValueMap vm = ValueMap.of("field=value");
		
		assertEquals("value", vm.getValue("field").orElse(""));
		
	}

	@Test
	void testCreationWithFieldNameList() {
		
		List<String> compoundFieldNameList = new ArrayList<>();
		
		compoundFieldNameList.add("emperors");
		compoundFieldNameList.add("3");
		compoundFieldNameList.add("nomen");
		
		ValueMap vmRoot = ValueMap.of(compoundFieldNameList, "Augustus");
		assertTrue(vmRoot.containsField("emperors"));
	   
		ValueMap vmChild1 = vmRoot.getValueMap("emperors").orElse(ValueMap.empty());
		assertFalse(vmChild1.isEmpty());
		assertTrue(vmChild1.containsField("3"));
		
		ValueMap vmChild2 = vmChild1.getValueMap("3").orElse(ValueMap.empty());
		assertFalse(vmChild2.isEmpty());
		assertTrue(vmChild2.containsField("nomen"));
		
		
		assertEquals("Augustus", vmChild2.getValue("nomen").orElse(""));
		
	}
	
	
	@Test
	void testCreationWithCompoundFieldnameString() {
		ValueMap vmRoot = ValueMap.of("field1.field2.field3=value");
		assertTrue(vmRoot.containsField("field1"));
		
		ValueMap vmChild1 = vmRoot.getValueMap("field1").orElse(ValueMap.empty());
		assertFalse(vmChild1.isEmpty());
		assertTrue(vmChild1.containsField("field2"));
		
		ValueMap vmChild2 = vmChild1.getValueMap("field2").orElse(ValueMap.empty());
		assertFalse(vmChild2.isEmpty());
		assertTrue(vmChild2.containsField("field3"));
		assertEquals("value", vmChild2.getValue("field3").orElse(""));
		
		assertFalse(valueMap.containsField("field1.field2.anotherField"));
				
	}
	
	@Test
	void testCreationWithSimpleDataObject() throws Exception {
		String author = "Edward Gibbon";
		String title = "The History of the Decline and Fall of the Roman Empire";
		String source = "Wikipedia";
		URL sourceLink = new URL("https://en.wikipedia.org/wiki/The_History_of_the_Decline_and_Fall_of_the_Roman_Empire");
		int id = 2021;
		
		Work work = new Work();
		work.setAuthor(author);
		work.setTitle(title);
		work.setSource(source);
		work.setSourceLink(sourceLink);
		work.setId(id);
		
		ValueMap valueMap = ValueMap.from(work);
		
		assertNotNull(valueMap);
		assertEquals(author, valueMap.getValue("author").orElse(""));
		assertEquals(title, valueMap.getValue("title").orElse(""));
		assertEquals(source, valueMap.getValue("source").orElse(""));
		assertEquals(sourceLink, valueMap.getValue("sourceLink").orElse(""));
		assertEquals(id, valueMap.getValue("id").orElse(""));
		
		
	}
	
	@Test
	void testCreationWithNestedDataObject() throws Exception {
		String author = "Dan Brown";
		String title = "The Novels of Dan Brown";
        int id = 8;		
        
        Reference[] references = {
        		new Reference("Digital Fortress", new URL("https://www.worldcat.org/title/digital-fortress/oclc/55045760")),
        		new Reference("Angels & Demons", new URL("https://www.worldcat.org/title/angels-demons/oclc/52990309")),
        		new Reference("Deception Point", new URL("https://www.worldcat.org/title/deception-point/oclc/47625287"))
        };
		
		Works works = new Works();
		works.setAuthor(author);
		works.setTitle(title);
		works.setId(id);
		
		works.addReference(references[0]);
		works.addReference(references[1]);
		works.addReference(references[2]);
		
		ValueMap valueMap = ValueMap.from(works);
		
		assertNotNull(valueMap);
		assertEquals(author, valueMap.getValue("author").orElse(""));
		assertEquals(title, valueMap.getValue("title").orElse(""));
		
		assertEquals(3, valueMap.getValueMaps("references").size());
		assertEquals(references[0].getTitle(), valueMap.getValue("references.0.title").orElse(""));
		assertEquals(references[0].getLink(), valueMap.getValue("references.0.link").orElse(""));
		assertEquals(references[1].getTitle(), valueMap.getValue("references.1.title").orElse(""));
		assertEquals(references[1].getLink(), valueMap.getValue("references.1.link").orElse(""));
		assertEquals(references[2].getTitle(), valueMap.getValue("references.2.title").orElse(""));
		assertEquals(references[2].getLink(), valueMap.getValue("references.2.link").orElse(""));
		
	}
	
		
	@Test
	void testCreationWithSimpleObjectIterator() throws Exception {
		List<String> list = Arrays.asList(new String[]{"Asimov", "Clark", "Pohl"});
		
		ValueMap valueMap = ValueMap.from(list.iterator());
		
		assertEquals("Asimov", valueMap.getValue("0").orElse(""));
		assertEquals("Clark", valueMap.getValue("1").orElse(""));
		assertEquals("Pohl", valueMap.getValue("2").orElse(""));
		
	}
	
	@Test 
	void testCreationWithComplexObjectIterator() throws Exception {
		Work work0 = new Work(); 
	    work0.setTitle("I Robot");
	    Work work1 = new Work(); 
	    work1.setTitle("Foundation");
	    Work work2 = new Work(); 
	    work2.setTitle("Foundation and Empire");
	    Work work3 = new Work(); 
	    work3.setTitle("Second Foundation");
	    
	    List<Work> list = Arrays.asList(new Work[]{work0, work1, work2, work3});
	    
	    ValueMap valueMap = ValueMap.from(list.iterator());
	    
	    assertEquals("I Robot", valueMap.getValue("0.title").orElse(""));
	    assertEquals("Foundation", valueMap.getValue("1.title").orElse(""));
	    assertEquals("Foundation and Empire", valueMap.getValue("2.title").orElse(""));
	    assertEquals("Second Foundation", valueMap.getValue("3.title").orElse(""));
		      
	}
	
	@Test 
	void testToObjectSimple() throws Exception {
		String author = "Edward Gibbon";
		String title = "The History of the Decline and Fall of the Roman Empire";
		String source = "Wikipedia";
		URL sourceLink = new URL("https://en.wikipedia.org/wiki/The_History_of_the_Decline_and_Fall_of_the_Roman_Empire");
		int id = 2021;
		
		ValueMap vm = new ValueMap();
		vm.put("author", author);
		vm.put("title", title);
		vm.put("source", source);
		vm.put("sourceLink", sourceLink);
		vm.put("id", id);
		
		Work work = (Work) vm.toObject(Work.class);
		
		assertEquals(author, work.getAuthor());
		assertEquals(title, work.getTitle());
		assertEquals(source, work.getSource());
		assertEquals(sourceLink, work.getSourceLink());
		assertEquals(id, work.getId());
		
	}
	
	@Test 
	void testToObjectWithList() throws Exception {
		ValueMap sourceValueMap = new ValueMap();
		sourceValueMap.put("id", 2021);
		sourceValueMap.put("title", "Dieter Bohlen - Seine gr��ten Hits");
		sourceValueMap.put("author", "Dieter Bohlen");
				
		sourceValueMap.put("references.0.title", "You're My Heart, You're My Soul");
		sourceValueMap.put("references.0.link", new URL("https://en.wikipedia.org/wiki/You%27re_My_Heart,_You%27re_My_Soul"));
			
		sourceValueMap.put("references.1.title", "You Can Win If You Want");
		sourceValueMap.put("references.1.link", new URL("https://en.wikipedia.org/wiki/You_Can_Win_If_You_Want"));
		
		sourceValueMap.put("references.2.title", "Cheri, Cheri Lady");
		sourceValueMap.put("references.2.link", new URL("https://en.wikipedia.org/wiki/Cheri,_Cheri_Lady"));
	
		
		Works works = (Works) sourceValueMap.toObject(Works.class);
		
		assertEquals(2021, works.getId());
		assertEquals("Dieter Bohlen - Seine gr��ten Hits", works.getTitle());
		assertEquals("Dieter Bohlen", works.getAuthor());
		
		assertEquals("You're My Heart, You're My Soul", works.getReference(0).getTitle());
		assertEquals("https://en.wikipedia.org/wiki/You%27re_My_Heart,_You%27re_My_Soul", works.getReference(0).getLink().toString());
		
		assertEquals("You Can Win If You Want", works.getReference(1).getTitle());
		assertEquals("https://en.wikipedia.org/wiki/You_Can_Win_If_You_Want", works.getReference(1).getLink().toString());
		
		assertEquals("Cheri, Cheri Lady", works.getReference(2).getTitle());
		assertEquals("https://en.wikipedia.org/wiki/Cheri,_Cheri_Lady", works.getReference(2).getLink().toString());
		
		
		
	}
	
	@Test 
	void testToObjectError() {
		ValueMap vm = new ValueMap();
		vm.put("unkownTestField", "Unknown test field value");
		
		assertThrows(ConversionException.class, () -> vm.toObject(Work.class));
		
	}


}
