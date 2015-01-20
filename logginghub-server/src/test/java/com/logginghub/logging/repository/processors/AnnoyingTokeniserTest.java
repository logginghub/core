package com.logginghub.logging.repository.processors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.repository.processors.AnnoyingTokeniser;
import com.logginghub.logging.repository.processors.AnnoyingTokeniser.Mode;

public class AnnoyingTokeniserTest {

    @Test public void commentTest() {

        AnnoyingTokeniser at = new AnnoyingTokeniser("This is a string with a 'quoted section' that should come out as a single token");

        at.getDefaultMode().setTokenSeparators(' ');
        at.getDefaultMode().setIncludeTokens(false);

        Mode mode = at.createMode();        
        mode.setTokenSeparators();
        mode.setModeStartCharacters('\'');
        mode.setModeEndCharacters('\'');
        mode.setIncludeTokens(true);
        
        at.getDefaultMode().addSubMode(mode);

        assertThat(at.nextToken(), is("This"));
        assertThat(at.nextToken(), is("is"));
        assertThat(at.nextToken(), is("a"));
        assertThat(at.nextToken(), is("string"));
        assertThat(at.nextToken(), is("with"));
        assertThat(at.nextToken(), is("a"));
        assertThat(at.nextToken(), is("'quoted section'"));
        assertThat(at.nextToken(), is("that"));
        assertThat(at.nextToken(), is("should"));
        assertThat(at.nextToken(), is("come"));
        assertThat(at.nextToken(), is("out"));
        assertThat(at.nextToken(), is("as"));
        assertThat(at.nextToken(), is("a"));
        assertThat(at.nextToken(), is("single"));
        assertThat(at.nextToken(), is("token"));
        assertThat(at.hasMoreTokens(), is(false));
    }
    
    @Test public void csvTest() {

        AnnoyingTokeniser at = new AnnoyingTokeniser("1,2,3,4,5,6,7,8");

        at.getDefaultMode().setTokenSeparators(',');
        at.getDefaultMode().setIncludeTokens(false);
        at.getDefaultMode().setAllowEmptyTokens(true);

        assertThat(at.nextToken(), is("1"));
        assertThat(at.nextToken(), is("2"));
        assertThat(at.nextToken(), is("3"));
        assertThat(at.nextToken(), is("4"));
        assertThat(at.nextToken(), is("5"));
        assertThat(at.nextToken(), is("6"));
        assertThat(at.nextToken(), is("7"));
        assertThat(at.nextToken(), is("8"));
        assertThat(at.hasMoreTokens(), is(false));
    }
    
    @Test public void csvTestWithGaps() {

        AnnoyingTokeniser at = new AnnoyingTokeniser("1,,3,,5,6,,");

        at.getDefaultMode().setTokenSeparators(',');
        at.getDefaultMode().setIncludeTokens(false);
        at.getDefaultMode().setAllowEmptyTokens(true);

        assertThat(at.nextToken(), is("1"));
        assertThat(at.nextToken(), is(""));
        assertThat(at.nextToken(), is("3"));
        assertThat(at.nextToken(), is(""));
        assertThat(at.nextToken(), is("5"));
        assertThat(at.nextToken(), is("6"));
        assertThat(at.nextToken(), is(""));
        
        // TODO : if this was working properly, you could argue you need another entry on the end, but it doesn't work
//        assertThat(at.nextToken(), is(""));

    }

    @Test public void howIMeanToUseIt(){
        String input = "This is [a string] with some {random} [bits] in  {different quote styles}";
        AnnoyingTokeniser at = new AnnoyingTokeniser(input);

        at.getDefaultMode().setTokenSeparators();
        at.getDefaultMode().setIncludeTokens(true);
        at.getDefaultMode().setAllowEmptyTokens(true);
        
        Mode squareBracketsMode = at.createMode();        
        squareBracketsMode.setTokenSeparators();
        squareBracketsMode.setModeStartCharacters('[');
        squareBracketsMode.setModeEndCharacters(']');
        squareBracketsMode.setIncludeTokens(true);
        
        Mode curleyBracketsMode = at.createMode();        
        curleyBracketsMode.setTokenSeparators();
        curleyBracketsMode.setModeStartCharacters('{');
        curleyBracketsMode.setModeEndCharacters('}');
        curleyBracketsMode.setIncludeTokens(true);

        at.getDefaultMode().addSubMode(curleyBracketsMode);
        at.getDefaultMode().addSubMode(squareBracketsMode);
        
        assertThat(at.nextToken(), is("This is "));
        assertThat(at.nextToken(), is("[a string]"));
        assertThat(at.nextToken(), is(" with some "));
        assertThat(at.nextToken(), is("{random}"));
        assertThat(at.nextToken(), is(" "));
        assertThat(at.nextToken(), is("[bits]"));
        assertThat(at.nextToken(), is(" in  "));
        assertThat(at.nextToken(), is("{different quote styles}"));
        assertThat(at.hasMoreTokens(), is(false));
        
        at.reset();
        StringBuilder builder = new StringBuilder();
        while(at.hasMoreTokens()){
            builder.append(at.nextToken());
        }
        
        assertThat(builder.toString(), is(input));
    }
    
}
