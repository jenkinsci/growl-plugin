package hudson.plugins.growl.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import hudson.plugins.growl.util.Cleaner;

import org.junit.Test;

public class CleanerTest {

	@Test
	public void toBooleanShouldReturnTrueForTrueValue() {
		assertThat(Cleaner.toBoolean("true"), equalTo(Boolean.TRUE)); 
	}


	@Test
	public void toBooleanShouldReturnTrueForYesValue() {
		assertThat(Cleaner.toBoolean("Yes"), equalTo(Boolean.TRUE)); 
	}

	@Test
	public void toBooleanShouldReturnFalseForFalseValue() {
		assertThat(Cleaner.toBoolean("false"), equalTo(Boolean.FALSE)); 
	}

	@Test
	public void toBooleanShouldReturnFalseForNoValue() {
		assertThat(Cleaner.toBoolean("No"), equalTo(Boolean.FALSE)); 
	}
	
	@Test
	public void toBooleanShouldReturnNullForInvalidValuesValue() {
		assertThat(Cleaner.toBoolean("Noasda"), equalTo(null)); 
		assertThat(Cleaner.toBoolean("123"), equalTo(null)); 
	}
	
	@Test
	public void toStringShouldReturnNullStringForDefault(){
		assertThat(Cleaner.toString("(Default)"), equalTo(null));
	}
	
	@Test
	public void toStringShouldReturnNullForSystemDefault(){
		assertThat(Cleaner.toString("(System Default)"), equalTo(null));
	}
	
	@Test
	public void toStringShouldReturnValueForAnyValue(){
		String text = "Something";
		assertThat(Cleaner.toString(text), equalTo(text));
	}

}
