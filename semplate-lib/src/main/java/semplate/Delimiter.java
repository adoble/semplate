package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;

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

	@Override
	public String toString() { return "Delimiter [start=" + start + ", end=" + end + "]"; }

	

}
