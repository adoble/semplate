package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.regex.Pattern;

/** Handles, in uniform way, field delimiters in semantically annotated markkdown files.
 * 
 * @author Andrew Doble
 *
 */
class Delimiter {
    private Optional<String> start = Optional.empty();
	private Optional<String> end = Optional.empty();

	Delimiter start(String startDelimiter) {
		this.start = filter(startDelimiter);
		return this;
	}

	Delimiter end(String endDelimiter) {
		this.end = filter(endDelimiter);
		return this;
	}

	private Optional<String> filter(String delimiterString) {
		return Optional.ofNullable(delimiterString).filter(s -> s.length() > 0);
	}


	Optional<String> start() { return start; }
	Optional<String> end() { return end; }
	
	Delimiter pair(String delimiterPair) {
		checkArgument(delimiterPair.length() == 2, "Only 2 characters are allowed in a delimiter pair - instead %s was specified.", delimiterPair);
		return this.start(delimiterPair.substring(0, 1)).end(delimiterPair.substring(1));
	}
	
	
	Delimiter insert(Delimiter insertedDelimiter) {
		start(this.start()
				  .map(s -> s + insertedDelimiter.start().orElse(""))
				  .orElse(insertedDelimiter.start().orElse(""))
		     );
		
		end(this.end()
				  .map(s -> insertedDelimiter.end().orElse("") + s)
				  .orElse(insertedDelimiter.end().orElse(""))
		     );
		
		return this;
		
	}
	
	/** Returns a Pattern object that matches text between the two delimiters. The delimiters are included in the result. 
	 *
	 * The pattern object matches the following - simplified - regex (s is the start delimiter, 
	 * e is the end delimiter )
	 * 
	 * <code>
	 *       s.*?e     start and end delimiters defined
	 *       s.*?$     no end delimiter defined
	 *       ^.*?e     no start delimiter defined
	 *       .*?       no start and end delimiter defined, i.e returns the whole line
	 *  </code>
	 *  
	 * @returns A Pattern object 
	 * 
	 */
	 Pattern pattern() {
		
		String spec =  this.start().map(s -> Pattern.quote(s)).orElse("^") 
				+ ".*?" 
				+ this.end.map(e -> Pattern.quote(e)).orElse("$");
		
		Pattern p = Pattern.compile(spec);

		return p;

	}
	
	/**  Tests if the supplied delimiter is equal to this delimiter. 
	 * 
	 * Equals is defined as both are empty of the start and end delimiters are the same. 
	 * 
	 * @param obj The delimiter object to be tested
	 * @return True if the delimiters are equals
	 */
	@Override
	public boolean equals(Object obj) {
		//return this.start().equals(testDelimiter.start()) && this.end().equals(testDelimiter.end());
		
		if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        Delimiter testDelimiter = (Delimiter) obj;

        return this.start().equals(testDelimiter.start()) && this.end().equals(testDelimiter.end());
}

	@Override
	public String toString() { 
		return "Delimiter [start=" + start + ", end=" + end + "]"; 
	}

	

}
