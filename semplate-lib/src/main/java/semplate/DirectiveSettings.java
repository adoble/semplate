package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;

public class DirectiveSettings {
    
	private Optional<String> commentStartDelimiter;
	private Optional<String> commentEndDelimiter;
	private Optional<String> startDelimiter;
	private Optional<String> endDelimiter;

	public DirectiveSettings() {
		commentStartDelimiter = Optional.empty();
		commentEndDelimiter = Optional.empty();
		startDelimiter = Optional.empty();
		endDelimiter = Optional.empty();
	}


	
	public void delimiterPair(String pair)  {
		checkArgument(pair.length() == 2, "The following delimiter pairs was specfied \"%s\", but it it does not contain exactly 2 characters", pair);

		startDelimiter = Optional.ofNullable(pair.substring(0, 1));
		endDelimiter = Optional.ofNullable(pair.substring(1));
	}



	public Optional<String> commentStartDelimiter() {
		return commentStartDelimiter;
	}


	public void commentStartDelimiter(String commentStartDelimiter) {
		this.commentStartDelimiter = Optional.ofNullable(commentStartDelimiter).filter(s -> s.length() > 0);
	}

	public Optional<String> commentEndDelimiter() {
		return commentEndDelimiter;
	}


	public void commentEndDelimiter(String commentEndDelimiter) {
		this.commentEndDelimiter = Optional.ofNullable(commentEndDelimiter).filter(s -> s.length() > 0);
	}

	public Optional<String> startDelimiter() {
		return startDelimiter;
	}



	public void startDelimiter(String startDelimiter) {
		this.startDelimiter = Optional.ofNullable(startDelimiter).filter(s -> s.length() > 0);
	}


	public Optional<String> endDelimiter() {
		return endDelimiter;
	}


	public void endDelimiter(String endDelimiter) {
		this.endDelimiter = Optional.ofNullable(endDelimiter).filter(s -> s.length() > 0);
	}



	@Override
	public String toString() {
		return "DirectiveSettings [commentStartDelimiter=" + commentStartDelimiter + ", commentEndDelimiter="
				+ commentEndDelimiter + ", startDelimiter=" + startDelimiter + ", endDelimiter=" + endDelimiter + "]";
	}

	



}
