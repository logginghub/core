package com.logginghub.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that does stuff to strings to try and resolve common regex patterns from them. It also
 * does a lot of faffing with logging replacement string style stuff (replacing variables with $1, $2 etc) but
 * I think these maybe inefficient when compared to pure regex matching, so they may need refactoring later.
 * @author admin
 *
 */
public class ValueStripper
{
    private List<String> m_unmatchedStrings = new ArrayList<String>();
    private List<String> m_replacementStrings = new ArrayList<String>();

    public void process(String string)
    {
        if(matchesExistingReplacementString(string))
        {
            // Already know about this one
        }
        else
        {
            boolean match = false;
            Iterator<String> iter = m_unmatchedStrings.iterator();
            while(iter.hasNext() && !match)
            {
                String unmatched = iter.next();

                String advancedReplacementString = ValueStripper.getReplacementString(string,
                                                                                      unmatched);

                if(advancedReplacementString != null)
                {
                    // Woot, a match
                    m_replacementStrings.add(advancedReplacementString);
                    iter.remove();
                    match = true;
                }
            }

            if(!match)
            {
                m_unmatchedStrings.add(string);
            }
        }
    }

    private boolean matchesExistingReplacementString(String string)
    {
        boolean matches = false;

        for(String replacementString : m_replacementStrings)
        {
            if(matchesReplacementString(string, replacementString))
            {
                // Fine
                matches = true;
            }
            else
            {
                matches = false;
                break;
            }
        }

        return matches;
    }

    /**
     * Try and make a replacement string buy analysing the similarities between
     * the strings.
     * 
     * @param a
     * @param b
     * @return Either a replacement string that matches both a and b, or null if
     *         nothing matched
     */
    private static String getReplacementString(String a, String b)
    {
        String replacementString;

        String[] aSplit = a.split(" ");
        String[] bSplit = b.split(" ");

        if(aSplit.length == bSplit.length)
        {
            StringBuilder builder = new StringBuilder();
            int matches = 0;
            int replacementIndex = 1;

            int length = aSplit.length;
            for(int i = 0; i < length; i++)
            {
                String aWord = aSplit[i];
                String bWord = bSplit[i];

                if(aWord.equals(bWord))
                {
                    matches++;
                    builder.append(aWord);
                }
                else
                {
                    builder.append("$");
                    builder.append(Integer.toString(replacementIndex));
                    replacementIndex++;
                }

                if(i < length - 1)
                {
                    builder.append(" ");
                }
            }

            // jshaw - lets say there is a match if more strings match than had
            // to be replaced for now,
            // this will need to be made a lot smarter later
            int replaces = replacementIndex - 1;
            if(matches > replaces)
            {
                replacementString = builder.toString();
            }
            else
            {
                replacementString = null;
            }
        }
        else
        {
            replacementString = null;
        }

        return replacementString;
    }

    public String[] getCurrentReplacementStrings()
    {
        return m_replacementStrings.toArray(new String[0]);
    }

    /**
     * Identify any numeric fields in the string and return a regular expression
     * that will match it.
     * 
     * @param string
     * @return
     */
    public static String getRegex(String string)
    {
        String[] split = string.split(" ");

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < split.length; i++)
        {
            String word = split[i];

            if(isAllAlpha(word))
            {
                builder.append(word);
            }
            else
            {
                builder.append(makeRegex(word));
            }

            if(i < split.length - 1)
            {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    private static String makeRegex(String word)
    {
        String regex = "[0-9,\\.]+";
        return regex;
    }

    private static boolean isAllAlpha(String word)
    {
        boolean isAllAlpha = true;

        int length = word.length();
        for(int i = 0; i < length; i++)
        {
            char charAt = word.charAt(i);
            if(Character.isDigit(charAt))
            {
                isAllAlpha = false;
                break;
            }
        }

        return isAllAlpha;
    }

    public static String getReplacementString(String string)
    {
        String[] split = string.split(" ");

        StringBuilder builder = new StringBuilder();

        int itemIndex = 1;

        for(int i = 0; i < split.length; i++)
        {
            String word = split[i];

            if(isAllAlpha(word))
            {
                builder.append(word);
            }
            else
            {
                builder.append("$" + Integer.toString(itemIndex));
                itemIndex++;
            }

            if(i < split.length - 1)
            {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static boolean matches(String string, String regex)
    {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(string);
        boolean matchFound = matcher.matches();
        return matchFound;
    }

    public static String[] getValues(String string, String replacementString)
    {
        String[] stringSplit = string.split(" ");
        String[] replacementStringSplit = replacementString.split(" ");

        if(stringSplit.length != replacementStringSplit.length)
        {
            throw new RuntimeException("It doesn't look like the replacement string '" + replacementString +
                                       "' is a match for input string '" +
                                       string +
                                       "'");
        }

        List<String> valuesList = new ArrayList<String>();
        for(int i = 0; i < replacementStringSplit.length; i++)
        {
            String replacementWord = replacementStringSplit[i];
            if(replacementWord.charAt(0) == '$')
            {
                String value = stringSplit[i];
                valuesList.add(value);
            }
        }

        String[] values = valuesList.toArray(new String[0]);
        return values;
    }

    public static boolean matchesReplacementString(String string, String replacementString)
    {
        String[] stringSplit = string.split(" ");
        String[] replacementStringSplit = replacementString.split(" ");

        boolean matches = true;

        if(stringSplit.length != replacementStringSplit.length)
        {
            matches = false;
        }
        else
        {
            List<String> valuesList = new ArrayList<String>();
            for(int i = 0; i < replacementStringSplit.length && matches; i++)
            {
                String word = stringSplit[i];
                String replacementWord = replacementStringSplit[i];

                if(replacementWord.charAt(0) == '$')
                {
                    // Ignore this one
                }
                else
                {
                    matches = word.equals(replacementWord);
                }
            }
        }
        
        return matches;
    }
}
