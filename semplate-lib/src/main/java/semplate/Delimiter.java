package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

/** Handles, in uniform way, field delimiters in semantically annotated markkdown files.
 * 
 * @author Andrew Doble
 *
 */
class Delimiter {
    private Optional<String> start = Optional.empty();
	private Optional<String> end = Optional.empty();
	
	
	/**
	 * Factory method to parse a line of text and create a comment delimiter based on the comment directive there. 
	 * 
	 * Precondition: The line needs to contain a comment directive 
	 * 
	 * @param line A string containing a comment directive
	 * @return A delimiter object with the comment delimiter(s)
	 */
	static Delimiter createCommentDelimiter(String line) {
		checkArgument(Patterns.COMMENT_DIRECTIVE_PATTERN.asPredicate().test(line), "The line \"%s\" does not contain a template comment field", line);
        
        Delimiter delimiter = new Delimiter();
		
        List<String> preamble = Splitter.on("{@").trimResults().splitToList(line);
		delimiter.start(preamble.get(0));

		List<String> postamble = Splitter.on("}}").splitToList(line);
		delimiter.end(postamble.get(1));

		return delimiter;
		
	}
	
	/** Factory method to parse a string containing a delimiter specification and 
	 * create a delimiter object. 
	 * 
	 * @param line The string containing the delimiter directive
	 * @return A Delimiter object 
	 */
	static Delimiter createDelimiter(String line) {
		Matcher matcher = Patterns.DELIMITER_DIRECTIVE_PATTERN.matcher(line);
		
		Delimiter delimiter = new Delimiter();  
		while(matcher.find()) {
			
		    // What type of delimiter directive is this?
		    String delimiterType = matcher.group("type");
		    String delimiterValue = matcher.group("delim");
		    if (delimiterValue.startsWith("\"")  && delimiterValue.endsWith("\"")) {
		      // Remove the quotes
		       delimiterValue = delimiterValue.substring(1, delimiterValue.length() - 1);
		       if (delimiterType.equals("start")) {
					delimiter.start(delimiterValue);
				} else if (delimiterType.equals("end")) {
					delimiter.end(delimiterValue);
				} else if (delimiterType.equals("pair")) {
					delimiter.pair(delimiterValue);
				}
				
		    } else {
		    	// This is reserved for special delimiters  such  as URL or DATE
		    	assert(false);  //TODO 
		    }
		    
		}
		
		return delimiter;
		
	}

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
