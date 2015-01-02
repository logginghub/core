package com.logginghub.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class StringUtilsTokeniser {

    private String string;
    private int pointer = 0;

    public StringUtilsTokeniser(String string) {
        this.string = string;
    }

    @Override public String toString() {
        return string.substring(pointer, string.length());
    }

    public boolean hasMore() {
        return pointer < string.length();

    }
    
    public int getPointer() {
        return pointer;
    }    

    public String upToAndIncluding(String search) {
        int index = string.indexOf(search, pointer);
        if (index == 1) {
            throw new IllegalArgumentException(StringUtils.format("Couldn't find '{}' in string '{}' passed index {}", search, this.string, pointer));
        }

        index += 1;
        String found = string.substring(pointer, index);
        pointer = index;
        return found;
    }

    public String upTo(String search) {

        int index = string.indexOf(search, pointer);
        if (index == -1) {
            throw new IllegalArgumentException(StringUtils.format("Couldn't find '{}' in string '{}' passed index {}", search, this.string, pointer));
        }

        String found = string.substring(pointer, index);
        pointer = index;
        return found;
    }

    public String next(char delim) {
        return upToOutsideQuotes(delim, true);
    }

    public String upToOutsideQuotes(char search) {
        return upToOutsideQuotes(search, false);
    }

    /**
     * Return the next bit of the string up to the first instance of the character provided, but
     * ignore it if the value is inside quotes. If the tokeniser hits the end of the string, it'll
     * return whatever it found, regardless of if the search character was found.
     * 
     * @param search
     * @return
     */
    public String upToOutsideQuotes(char search, boolean discardDelim) {
        Set<Character> quotes = new HashSet<Character>();
        quotes.add('\'');
        quotes.add('\"');
        return upToOutsideQuotes(search, quotes, discardDelim);
    }

    /**
     * Return the next bit of the string up to the first instance of the character provided, but
     * ignore it if the value is inside quotes. If the tokeniser hits the end of the string, it'll
     * return whatever it found, regardless of if the search character was found.
     * 
     * @param search
     * @return
     */
    public String upToOutsideQuotes(char search, Set<Character> allowedQuoteChars, boolean discardDelim) {

        boolean done = pointer >= string.length();

        StringBuilder builder = new StringBuilder();
        int searchPointer = pointer;

        Stack<Character> quoteStack = new Stack<Character>();

        while (!done) {
            char c = string.charAt(searchPointer);
            if (allowedQuoteChars.contains(c)) {

                if (quoteStack.size() == 0) {
                    // First entry
                    quoteStack.push(c);
                }
                else {
                    if (quoteStack.peek() == c) {
                        // Assume a closing quote as they are matched
                        quoteStack.pop();
                    }
                    else {
                        // Different quote style, push it
                        quoteStack.push(c);
                    }
                }

                builder.append(c);
            }
            else {
                if (c == search && quoteStack.isEmpty()) {
                    done = true;
                    if (discardDelim) {
                        searchPointer++;
                    }
                }
                else {
                    builder.append(c);
                }
            }

            if (!done) {
                searchPointer++;
            }

            if (searchPointer == string.length()) {
                done = true;
            }
        }

        pointer = searchPointer;
        return builder.toString();
    }

    public String nextQuotedWordWithQuotesRemoved(char delim, char allowedQuote) {
        Set<Character> quotes = new HashSet<Character>();
        quotes.add(allowedQuote);
        String word = upToOutsideQuotes(delim, quotes, true);
        String noQuotes = StringUtils.unquote(word);
        return noQuotes;
    }

    public String nextQuotedWordWithQuotesRemoved(char c) {
        String word = nextQuotedWord(c);
        String noQuotes = StringUtils.unquote(word);
        return noQuotes;
    }

    public void skip() {
        pointer++;
    }

    public char nextChar() {
        if (pointer < string.length()) {
            return string.charAt(pointer++);
        }
        else {
            throw new IllegalArgumentException("Out of characters");
        }
    }

    public String nextXChars(int x) {
        String sub = string.substring(pointer, pointer + x);
        pointer += x;
        return sub;

    }

    public String restOfString() {
        String sub = string.substring(pointer, string.length());
        pointer = string.length();
        return sub;
    }

    public String peekNextWord() {
        String nextWord;
        if (hasMore()) {
            int index = string.indexOf(' ', pointer);
            if (index == -1) {
                nextWord = string.substring(pointer, string.length());
            }
            else {
                nextWord = string.substring(pointer, index);
            }
            return nextWord;
        }
        else {
            return "";
        }

    }

    public void skipWord() {
        int index = string.indexOf(' ', pointer);
        if (index == -1) {
            pointer = string.length();
        }
        else {
            pointer = index + 1;
        }
    }

    public String nextWord() {
        String nextWord;

        if (string.charAt(pointer) == '\'' || string.charAt(pointer) == '\"') {
            nextWord = nextQuotedWord();
            
            // Move passed the space
            pointer++;
        }
        else {
            int index = string.indexOf(' ', pointer);
            if (index == -1) {
                nextWord = string.substring(pointer, string.length());
                pointer = string.length();
            }
            else {
                nextWord = string.substring(pointer, index);
                pointer = index + 1;
            }
        }
        return nextWord;

    }

    public String nextQuotedWord() {
        return upToOutsideQuotes(' ');
    }

    public String nextQuotedWordWithQuotesRemoved() {
        return StringUtils.unquote(upToOutsideQuotes(' '));
    }

    public String nextQuotedWord(char delim) {
        return upToOutsideQuotes(delim);
    }

    public String nextWordUpTo(String... operators) {

        int nearest = Integer.MAX_VALUE;

        for (String operator : operators) {
            int index = string.indexOf(operator, pointer);
            if (index != -1) {
                nearest = Math.min(nearest, index);
            }
        }

        String word = string.substring(pointer, nearest);
        pointer += word.length();

        if (peekChar() == ' ') {
            skip();
        }

        return word.trim();
    }

    public String nextWordFrom(String... operators) {
        int largest = 0;

        for (String operator : operators) {
            if (isNext(operator)) {
                int index = string.indexOf(operator, pointer);
                largest = Math.max(largest, index + operator.length());
            }
        }

        String word = string.substring(pointer, largest);
        pointer += word.length();
        if (peekChar() == ' ') {
            skip();
        }

        return word;

    }

    public char peekChar() {
        return string.charAt(pointer);
    }

    private boolean isNext(String operator) {
        return string.substring(pointer, string.length()).startsWith(operator);

    }

    public int nextInteger() {

        boolean done = false;

        StringBuilder builder = new StringBuilder();
        boolean insideNumber = false;
        while (!done) {
            char c = string.charAt(pointer);

            if (c == ' ') {
                if (!insideNumber) {
                    // Ignore leading whitespace
                    pointer++;
                }
                else {
                    done = true;
                }
            }
            else if (Character.isDigit(c)) {
                insideNumber = true;
                builder.append(c);
                pointer++;
            }
            else {
                done = true;
            }

            if (pointer == string.length()) {
                done = true;
            }
        }

        int result;
        if (builder.length() > 0) {
            result = Integer.parseInt(builder.toString());
        }
        else {
            throw new IllegalArgumentException("No integer was found at start of the string '" + peekRemaining());
        }

        return result;

    }

    public String peekRemaining() {
        return string.substring(pointer);

    }


    public enum CharacterType {
        numeric,
        alphaNumeric,
        whitespace,
        punctuation,
        other
    }

    public String nextUpToCharacterTypeChange() {
        SentanceCharacterTyper characterTyper = new SentanceCharacterTyper();
        return nextUpToCharacterTypeChange(characterTyper);
    }

    public String nextUpToCharacterTypeChange(CharacterTyper characterTyper) {

        char peekChar = peekChar();

        StringBuilder builder = new StringBuilder();

        CharacterType startingType = characterTyper.getCharacterType(peekChar);

        boolean done = false;

        while (!done) {
            char c = string.charAt(pointer);

            CharacterType characterType = characterTyper.getCharacterType(c);

            boolean okToAdd = false;

            if (characterType == startingType) {
                okToAdd = true;
            }
            else {
                // Allow dots in numbers
                // TODO : other regions might not like this...
                if (startingType == CharacterType.numeric && c == '.') {
                    if (string.length() >= pointer + 1) {
                        char after = string.charAt(pointer + 1);
                        CharacterType afterType = characterTyper.getCharacterType(after);

                        if (startingType == CharacterType.numeric && afterType == CharacterType.numeric) {
                            okToAdd = true;
                        }
                    }
                }

            }

            if (okToAdd) {
                builder.append(c);
                pointer++;

                // Dont allow runs of weird characters
                if (startingType == CharacterType.punctuation || startingType == CharacterType.other) {
                    done = true;
                }

                if (pointer == string.length()) {
                    done = true;
                }
            }
            else {
                done = true;
            }
        }

        String result = builder.toString();
        return result;
    }

    public interface CharacterTyper {
        CharacterType getCharacterType(char c);
    }

    public static final class SentanceCharacterTyper implements CharacterTyper {
        public CharacterType getCharacterType(char peekChar) {
            CharacterType characterType;

            switch (peekChar) {
                case '\'':
                case '.':
                case ',':
                case '\"':
                case '-':
                    characterType = CharacterType.punctuation;
                    break;
                case '\r':
                case '\n':
                case '\t':
                case ' ':
                    characterType = CharacterType.whitespace;
                    break;

                default:
                    if (Character.isDigit(peekChar)) {
                        characterType = CharacterType.numeric;
                    }
                    else if (Character.isLetter(peekChar) || Character.isDigit(peekChar)) {
                        characterType = CharacterType.alphaNumeric;
                    }
                    else {
                        characterType = CharacterType.other;
                    }
            }

            return characterType;
        }
    }

    public List<String> toList() {

        SentanceCharacterTyper characterTyper = new SentanceCharacterTyper();
        List<String> result = new ArrayList<String>();

        StringBuilder current = new StringBuilder();
        while (hasMore()) {
            char nextChar = nextChar();
            CharacterType characterType = characterTyper.getCharacterType(nextChar);
            if (characterType == CharacterType.whitespace) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                }
            }
            else {
                current.append(nextChar);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;

    }

}
