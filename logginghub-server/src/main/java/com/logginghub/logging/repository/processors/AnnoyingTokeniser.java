package com.logginghub.logging.repository.processors;

import java.util.Stack;

/**
 * Couldn't figure out another way to do this :/
 * 
 * @author James
 * 
 */
public class AnnoyingTokeniser {

    private final String expression;
    private int currentIndex = 0;
    private boolean complete = false;
    private Mode currentMode = null;
    private Mode defaultMode;
    private Stack<Mode> modeStack = new Stack<Mode>();
    
    /**
     * The mode for the current token, as the currentMode may have changed.
     */
    private Mode currentTokenMode;

    class Mode {
        char modeActivateChar = 0;
        char modeDeactivateChar = 0;

        boolean[] tokenSeparators = new boolean[255];

        boolean[] thisModeStartCharacters = new boolean[255];
        boolean[] thisModeEndCharacters = new boolean[255];

        Mode[] subModeStarters = new Mode[255];
        private boolean includeTokenCharacters = false;
        private boolean allowEmptyTokens;

        public Mode setTokenSeparators(char... separators) {

            for (int i = 0; i < tokenSeparators.length; i++) {
                tokenSeparators[i] = false;
            }

            for (char c : separators) {
                tokenSeparators[c] = true;
            }

            return this;
        }

        public void setModeStartCharacters(char... chars) {
            for (int i = 0; i < thisModeStartCharacters.length; i++) {
                thisModeStartCharacters[i] = false;
            }

            for (char c : chars) {
                thisModeStartCharacters[c] = true;
            }
        }

        public void setModeEndCharacters(char... chars) {
            for (int i = 0; i < thisModeEndCharacters.length; i++) {
                thisModeEndCharacters[i] = false;
            }

            for (char c : chars) {
                thisModeEndCharacters[c] = true;
            }
        }

        public void addSubMode(Mode subMode) {
            boolean[] subModeStartCharacters = subMode.thisModeStartCharacters;
            for (int i = 0; i < subModeStartCharacters.length; i++) {
                if (subModeStartCharacters[i]) {
                    subModeStarters[i] = subMode;
                }
            }
        }

        public void setIncludeTokens(boolean includeTokens) {
            this.includeTokenCharacters = includeTokens;
        }

        public void setAllowEmptyTokens(boolean allowEmptyTokens) {
            this.allowEmptyTokens = allowEmptyTokens;
        }

    }

    public Mode getDefaultMode() {
        return defaultMode;
    }

    // public void setDefaultTokens(char... tokens) {
    // for (char c : tokens) {
    // defaultTokens[c] = true;
    // }
    // }

    public AnnoyingTokeniser(String expression) {
        this.expression = expression;

        defaultMode = new Mode().setTokenSeparators(' ');
        currentMode = defaultMode;
    }

    public int getCurrentTokenType() {
        return 0;

    }

    public String nextToken() {

        StringBuilder currentToken = new StringBuilder();

        boolean done = false;
        while (!done) {
            char nextChar = expression.charAt(currentIndex);            

            if (currentMode.tokenSeparators[nextChar]) {
                // This is token marker

                if (currentToken.length() > 0) {
                    done = true;

                    if (currentMode.includeTokenCharacters) {
                        currentToken.append(nextChar);
                    }
                }
                else {
                    // This is just an empty token, do we need a setting to
                    // return it?
                    if(currentMode.allowEmptyTokens){
                        done = true;                                               
                    }
                }

            }
            else if (currentMode.subModeStarters[nextChar] != null) {
                Mode switchMode = currentMode.subModeStarters[nextChar];
                modeStack.push(currentMode);
                currentTokenMode = currentMode;
                currentMode = switchMode;

                if (currentToken.length() > 0) {
                    // We are done though, we need to send back the current
                    // token before processing the next mode
                    currentIndex--;
                    done = true;
                }
                else {
                    if (currentMode.includeTokenCharacters) {
                        currentToken.append(nextChar);
                    }

                }
            }
            else if (currentMode.thisModeEndCharacters[nextChar]) {
                // The current mode is done
                if (currentMode.includeTokenCharacters) {
                    currentToken.append(nextChar);
                }

                // Revert back to the previous one
                currentTokenMode = currentMode;
                currentMode = modeStack.pop();
                done = true;
            }
            else {
                currentToken.append(nextChar);
                currentTokenMode = currentMode;
            }

            currentIndex++;
            
            if (currentIndex == expression.length()) {
                done = true;
                complete = true;
            }
        }

        String token = currentToken.toString();
        return token;

    }

    public boolean hasMoreTokens() {
        return !complete;

    }

    public Mode createMode() {
        return new Mode();
    }

    public void reset() {
        currentIndex = 0;
        complete = false;
        modeStack.clear();
        currentMode = defaultMode;
    }

    public Mode getCurrentMode() {
        return currentMode;
         
    }

    public Mode getCurrentTokenMode() {
        return currentTokenMode;
         
    }

    // public void addTokenType(char tokenStartChar, char tokenEndChar, int
    // tokenType) {
    //
    // TokenType type = new TokenType();
    // type.tokenEndChar = tokenEndChar;
    // type.tokenStartChar = tokenStartChar;
    // type.tokenType = tokenType;
    //
    // defaultMode[tokenStartChar] = type;
    // defaultMode[tokenEndChar] = type;
    //
    // }

}
