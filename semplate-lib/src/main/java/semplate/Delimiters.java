package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.*;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;



/** Manages a list of delimiters  
 * 
 * @see Delimiter
 * 
 * @author Andrew Doble
 *
 */
public class Delimiters implements Iterable<Delimiter>, Cloneable {
		
	private ArrayList<Delimiter> delimiters = new ArrayList<>();
	
	/** Creates a Delimiter object with the specified start and end delimiter string.
	 * 
	 * @see Delimiter 
	 * 
	 * @param startDelimiter A string with the start delimiter
	 * @param endDelimiter A string with the end delimiter
	 */
	void add(String startDelimiter, String endDelimiter) {
		Delimiter delimiter = (new Delimiter()).start(startDelimiter).end(endDelimiter);
		add(delimiter);
	}
	
	/** Create a Delimiter object with the specified pair of single character delimiters.
	 * 
	 * @see Delimiter
	 * 
	 * @param delimiterPair A string with the start and end delimiter character pair, e.g "[]" or "()"
	 * @throws IllegalArgumentException if the delimiterPair string contains more then two characters. 
	 */
	void addPair(String delimiterPair) {
		checkArgument(delimiterPair.length() == 2, "Calling addPair with %s. Only 2 characters are allowed.", delimiterPair);

		add((new Delimiter()).pair(delimiterPair));
	}
	
	/** Append the specified Delimiter object to the list of delimiters.
	 * 
	 * @see Delimiter
	 * 
	 * @param delimiter The delimiter object to be added
	 */
	void add(Delimiter delimiter) {
		delimiters.add(delimiter);
		
	}
	
	/** Appends all of the specified delimiters to the end of this list, in the order 
	 *  that they are returned by the specified delimiters Iterator.
	 *  
	 *  @see Delimiter
	 *  
	 *  @param delimiters The Delimiters object to be appended.
	 */
	void add(Delimiters delimiters) {
	  	for (Delimiter delim: delimiters) {
	  		this.add(delim);
	  	}
	}
    
	/** The number of delimiters added.
	 * 
	 * @see Delimiter
	 * 
	 * @return An <code>int</code> of the number of delimiters.
	 */
	int number() {
		return delimiters.size();
	}
	
    
	/** Returns the start delimiter at the specified index.
	 * 
	 * @see Delimiter
	 * @see Optional
	 * 
	 * @param index The index of the delimiter
	 * @return An Optional string with the start delimiter or empty if there is no start delimiter.
	 */
	Optional<String> startDelimiter(int index) {
		return delimiters.get(index).start();
	}
    	
	/** Returns the end  delimiter at the specified index.
	 * 
	 * @see Delimiter
	 * @see Optional
	 * 
	 * @param index The index of the delimiter
	 * @return An Optional string with the end delimiter or empty if there is no end delimiter.
	 */
	Optional<String> endDelimiter(int index) {
		return delimiters.get(index).end();
	}

   
	/** An iterator over all the added Delimiter objects
	 *  @see Delimiter
	 */
	@Override
	public Iterator<Delimiter> iterator() {
		return delimiters.iterator();
	}

	/** Is a string  surrounded by one of the added delimiters?
	 * <p>
	 * If the following delimiters have been added:
	 * <ul>
	 * <li>    "(", ")"
	 * <li>    "[", "]"
	 * <li>    "//", ""
	 * </ul>
	 *     
	 * Then the following strings will return true: 
	 * <ul>
	 * <li>"(A link)"
	 * <li>"[A title]"
	 * <li>"// A long comment\n"
	 * </ul>
	 * <p>
	 * And the following will return false:
	 * <ul>
	 * <li> "No delimiters"
	 * <li>"(Malformed]"
	 * </ul>
	 * 
	 * @see Delimiter
	 * 
	 * @param s The string to be tested
	 * @return True if the string is delimiter with an added delimiter
	 */
	boolean isDelimited(String s) {
		if (s.length() < 2) return false;
		
		Optional<String> startChar  = Optional.of(s.substring(0, 1));
		Optional<String> endChar  = Optional.of(s.substring(s.length() - 1));
		
		for (Delimiter delim: this) {
			if (startChar.equals(delim.start()) && endChar.equals(delim.end())) {
				return true;
			}
		}
		return false;
	}
	
	Delimiters insertAll(String s, String e) {
		Delimiter insert = new Delimiter().start(s).end(e);
		
		delimiters.forEach(d -> d.insert(insert));
		
		return this; 
	}
	
	/** Constructs a pattern that matches all of the added delimiters. 
	 * 
	 * If no delimiters have been added then matched the whole text.
	 * 
	 * @return A Pattern object
	 */
	Pattern pattern() {
		ArrayList<String> patternSpecs = new ArrayList<String>();
		
		if (delimiters.size() > 0) {
		     delimiters.forEach(d -> patternSpecs.add(d.pattern().pattern()));
		} else {
			patternSpecs.add("^.*$");
		}
		
		String completeSpec = Joiner.on("|").join(patternSpecs); // 
		
		Pattern pattern = Pattern.compile(completeSpec);
		
		return pattern;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public Delimiters clone() throws CloneNotSupportedException {
			
		Delimiters clone = (Delimiters) super.clone();
		clone.delimiters = (ArrayList<Delimiter>) this.delimiters.clone();  // Deep copy
		
		return clone;
	}

	@Override
	public String toString() {
		return "Delimiters [delimiters=" + delimiters + "]";
	}





}
