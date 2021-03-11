package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.*;



public class Delimiters implements Iterable<Delimiter>{
		
	private ArrayList<Delimiter> delimiters = new ArrayList<>();
	
	public void add(String startDelimiter, String endDelimiter) {
		Delimiter delimiter = (new Delimiter()).start(startDelimiter).end(endDelimiter);
		add(delimiter);
	}
	
	public void addPair(String delimiterPair) {
		checkArgument(delimiterPair.length() == 2, "Calling addPair with %s. Only 2 characters are allowed.", delimiterPair);

		add((new Delimiter()).pair(delimiterPair));
	}
	
	public void add(Delimiter delimiter) {
		delimiters.add(delimiter);
		
	}
	
	/**Appends all of the specified delimiters to the end of this list, in the order 
	 * that they are returned by the specified delimiters Iterator.
	 */
	public void add(Delimiters delimiters) {
	  	for (Delimiter delim: delimiters) {
	  		this.add(delim);
	  	}
	}
    
	public int number() {
		return delimiters.size();
	}
	
    public Optional<String> startDelimiter(int index) {
		return delimiters.get(index).start();
	}
    	
    public Optional<String> endDelimiter(int index) {
		return delimiters.get(index).end();
	}

   
	@Override
	public Iterator<Delimiter> iterator() {
		return delimiters.iterator();
	}

	/* 
	 * Is the string s surrounded by one of the specified delimiters
	 * @param s The string to be tested
	 */
	public boolean suround(String s) {
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

	@Override
	public String toString() {
		return "Delimiters [delimiters=" + delimiters + "]";
	}





}
