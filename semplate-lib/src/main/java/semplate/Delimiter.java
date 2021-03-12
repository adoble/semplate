package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.regex.Pattern;

/** TODO
 * @author Andrew Doble
 *
 */
public class Delimiter {
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
	
	/* Returns a Pattern object that matches text between the two delimiters.
	 *
	 * The pattern object matches  the following - simplified - regex ( s is the start delimiter, e is the end delimiter)
	 * 
	 * <code>
	 *        s[^e]*e     start and end delimiters defined
	 *        [^e]*e      no start delimiter defined
	 *        s[^s]*      no end delimiter defined
	 *        ^.*$        no start and end delimiter defined, i.e returns the whole line
	 * </code>        
	 *	      
	 * @returns A Pattern object 
	 * 
	 */
	public Pattern pattern() {
		
		String spec =  this.start().map(s -> Pattern.quote(s)).orElse("") 
				+ "[" + this.end().map(e -> "^" + Pattern.quote(e)).orElse(this.start().map(s -> "^" + Pattern.quote(s)).orElse(".")) + "]*" 
				+ this.end().map(e -> Pattern.quote(e)).orElse("");
		
		// If no start and end delimiters are defined then create a pattern to match a whole line. 
		if (this.start.isEmpty() && this.end.isEmpty()) 
			spec = "^.*$";
		
		Pattern p = Pattern.compile(spec);

		return p;

	}

	@Override
	public String toString() { return "Delimiter [start=" + start + ", end=" + end + "]"; }

	

}
