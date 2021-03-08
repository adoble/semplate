package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.*;



public class Delimiters implements Iterable<Delimiters.Delimiter>{
	
	public static class Delimiter {
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
		
		
	}
    
	private Delimiter comment = new Delimiter();
	
	private ArrayList<Delimiter> delimiters = new ArrayList<>();
	
//	private Optional<String> commentStartDelimiter;
//	private Optional<String> commentEndDelimiter;
//	private Optional<String> startDelimiter;
//	private Optional<String> endDelimiter;

	public Delimiters() {
	  //TODO
	}

    
	
//	public void delimiterPair(String pair)  {
//		checkArgument(pair.length() == 2, "The following delimiter pairs was specfied \"%s\", but it it does not contain exactly 2 characters", pair);
//
//		startDelimiter = Optional.ofNullable(pair.substring(0, 1));
//		endDelimiter = Optional.ofNullable(pair.substring(1));
//	}


    
	

	public void add(String startDelimiter, String endDelimiter) {
		Delimiter delimiter = (new Delimiter()).start(startDelimiter).end(endDelimiter);
		delimiters.add(delimiter);
	}
	
    public void addPair(String delimiterPair) {
	checkArgument(delimiterPair.length() == 2, "Calling addPair with %s. Only2 charaters are allowed.", delimiterPair);
	
	Delimiter delimiter = (new Delimiter()).start(delimiterPair.substring(0, 1)).end(delimiterPair.substring(1));
	delimiters.add(delimiter);
	
	
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

    public Optional<String> commentStartDelimiter() {
		return comment.start();
	}


	public void commentStartDelimiter(String commentStartDelimiter) {
		this.comment.start(commentStartDelimiter);
	}

	public Optional<String> commentEndDelimiter() {
		return comment.end();
	}


	public void commentEndDelimiter(String commentEndDelimiter) {
		this.comment.end(commentEndDelimiter);
	}


	@Override
	public Iterator<Delimiter> iterator() {
		return delimiters.iterator();
	}





}
