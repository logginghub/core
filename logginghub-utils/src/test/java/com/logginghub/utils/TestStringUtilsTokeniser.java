package com.logginghub.utils;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestStringUtilsTokeniser {

    private StringUtilsTokeniser emptyTokeniser = new StringUtilsTokeniser("");
    private StringUtilsTokeniser singleWordTokeniser = new StringUtilsTokeniser("singleWordLine");

    private StringUtilsTokeniser st = new StringUtilsTokeniser("This is our test string with 'some bits' in quotes and some bits repeated some bits repeated");

    private StringUtilsTokeniser stWithOtherChars = new StringUtilsTokeniser("This\nis\tour\rtest\r\nstring \t with \n 'some bits' \n in\nquotes\nand\nsome \n bits \t repeated some bits repeated");
    
    @Test public void test_other_chars() {
        
        assertThat(stWithOtherChars.nextWord(), is("This"));
        assertThat(stWithOtherChars.nextWord(), is("is"));
        assertThat(stWithOtherChars.nextWord(), is("our"));
        assertThat(stWithOtherChars.nextWord(), is("test"));
        assertThat(stWithOtherChars.nextWord(), is("string"));
        assertThat(stWithOtherChars.nextWord(), is("with"));
        assertThat(stWithOtherChars.nextWord(), is("'some bits'"));
        assertThat(stWithOtherChars.nextWord(), is("in"));
        assertThat(stWithOtherChars.nextWord(), is("quotes"));
        assertThat(stWithOtherChars.nextWord(), is("and"));
        assertThat(stWithOtherChars.nextWord(), is("some"));
        assertThat(stWithOtherChars.nextWord(), is("bits"));
        assertThat(stWithOtherChars.nextWord(), is("repeated"));
        assertThat(stWithOtherChars.nextWord(), is("some"));
        assertThat(stWithOtherChars.nextWord(), is("bits"));
        assertThat(stWithOtherChars.nextWord(), is("repeated"));
        
    }

    @Test public void test_quotes() {

        assertThat(st.nextWord(), is("This"));
        assertThat(st.nextWord(), is("is"));
        assertThat(st.nextWord(), is("our"));
        assertThat(st.nextWord(), is("test"));
        assertThat(st.nextWord(), is("string"));
        assertThat(st.nextWord(), is("with"));
        assertThat(st.nextWord(), is("'some bits'"));
        assertThat(st.nextWord(), is("in"));

    }


    @Test public void testPeekNextWord() {
        assertThat(st.peekNextWord(), is("This"));
        assertThat(st.peekNextWord(), is("This"));
        
        assertThat(singleWordTokeniser.peekNextWord(), is("singleWordLine"));
        assertThat(emptyTokeniser.peekNextWord(), is(""));
    }

    
    @Test public void testSkipWord() {
        st.skipWord();
        assertThat(st.peekNextWord(), is("is"));

        stWithOtherChars.skipWord();
        assertThat(stWithOtherChars.peekNextWord(), is("is"));
        
        singleWordTokeniser.skipWord();
        assertThat(singleWordTokeniser.peekNextWord(), is(""));
        assertThat(singleWordTokeniser.hasMore(), is(false));

        emptyTokeniser.skipWord();
        assertThat(emptyTokeniser.peekNextWord(), is(""));
        assertThat(emptyTokeniser.hasMore(), is(false));
    }


    @Test
    public void testNextWordUpTo() {
        
        st = new StringUtilsTokeniser("a=b");
        assertThat(st.nextWordUpTo("="), is("a"));
        assertThat(st.nextWordFrom("="), is("="));
        assertThat(st.nextWord(), is("b"));

        st = new StringUtilsTokeniser("a==b");
        assertThat(st.nextWordUpTo("=", "=="), is("a"));
        assertThat(st.nextWordFrom("=", "=="), is("=="));
        assertThat(st.nextWord(), is("b"));
    }

    @Test
    public void testNextInteger() {
        assertThat(new StringUtilsTokeniser("999").nextInteger(), is(999));
        
        StringUtilsTokeniser st;
        st = new StringUtilsTokeniser(" 12.34foo");
        assertThat(st.nextInteger(), is(12));
        assertThat(st.nextXChars(3), is(".34"));
        assertThat(st.nextWord(), is("foo"));
        
        st = new StringUtilsTokeniser("  234345 foo");
        assertThat(st.nextInteger(), is(234345));
        assertThat(st.nextXChars(1), is(" "));
        assertThat(st.nextWord(), is("foo"));
    }

    @Test public void testUpToCharacterTypeChange() throws Exception {
        StringUtilsTokeniser tokeniser = new StringUtilsTokeniser("This,maybe is a 1.32 sentance 'asdfe-asdfe!' [23 monkey] with\r\nsome wacky \"this is all together\" stuff in it");
        
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("This"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(","));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("maybe"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("is"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("a"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("1.32"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("sentance"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("'"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("asdfe"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("-"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("asdfe"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("!"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("'"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("["));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("23"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("monkey"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("]"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("with"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("\r\n"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("some"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("wacky"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("\""));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("this"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("is"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("all"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("together"));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is("\""));
        assertThat(tokeniser.nextUpToCharacterTypeChange(), is(" "));
        assertThat(tokeniser.restOfString(), is("stuff in it"));
        
        
    }

    

    @Test public void testUpToOutsideQuotes() throws Exception {

        StringUtilsTokeniser st = new StringUtilsTokeniser("value=\"This is a 'string with a quoted mid' section\" another thing");
        
        assertThat(st.upTo("="), is("value"));
        st.skip();
        assertThat(st.upToOutsideQuotes(' '), is("\"This is a 'string with a quoted mid' section\""));
        assertThat(st.restOfString(), is(" another thing"));
        
        
        assertThat(new StringUtilsTokeniser("").upToOutsideQuotes('s'), is(""));
        assertThat(new StringUtilsTokeniser("aabb").upToOutsideQuotes('s'), is("aabb"));
        
    }


    @Test public void testToList() throws Exception {
        StringUtilsTokeniser st = new StringUtilsTokeniser(" r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa\r\n");
        List<String> list = st.toList();
        assertThat(list.get(0), is("r"));
        assertThat(list.get(1), is("b"));
        assertThat(list.get(2), is("swpd"));
        assertThat(list.get(3), is("free"));
        assertThat(list.get(4), is("buff"));
        assertThat(list.get(5), is("cache"));
        assertThat(list.get(6), is("si"));
        assertThat(list.get(7), is("so"));
        assertThat(list.get(8), is("bi"));
        assertThat(list.get(9), is("bo"));
        assertThat(list.get(10), is("in"));
        assertThat(list.get(11), is("cs"));
        assertThat(list.get(12), is("us"));
        assertThat(list.get(13), is("sy"));
        assertThat(list.get(14), is("id"));
        assertThat(list.get(15), is("wa"));
        
    }
}
