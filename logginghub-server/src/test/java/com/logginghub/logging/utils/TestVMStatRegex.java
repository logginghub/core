package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestVMStatRegex
{
    @Test public void test()
    {
        String input = "vmstat: 0  0      0 2501060 156960 566420    0    0     0     0   88  179  0  0 100  0";
        String regex = "vmstat:\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)";
        
        regex = "vmstat:\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)";
        input="vmstat: 0  0      0 2501184 156960 566420    0    0     0     0   89  190  0  0 100  0";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        
        assertThat(matcher.matches(), is(true));
//        if(matcher.matches()){
//            System.out.println("Matched");
//            for(int i = 0; i < matcher.groupCount(); i++){
//                System.out.println("Group " + i + " : '" + matcher.group(i+1) + "'");
//            }
//        }else{
//            System.out.println("Didn't match");
//        }
    }
}
