package com.logginghub.logging.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.repository.DataFileNameFactory;

public class TestDataFileNameFactory {
    @Test public void test() {

        String tempName = DataFileNameFactory.getWritingTemporaryFilename("prefix", 0);
        assertThat(tempName, is("prefix19700101.000000.logdata.writing"));
        
        String finishedName = DataFileNameFactory.getFinishedFilename("prefix", 0);
        assertThat(finishedName, is("prefix19700101.000000.logdata"));
        
        assertThat(DataFileNameFactory.extractDatePart("prefix",finishedName), is("19700101.000000"));
        assertThat(DataFileNameFactory.extractDatePart("prefix",tempName), is("19700101.000000"));

    }
    
    @Test public void test_empty_prefix() {

        String tempName = DataFileNameFactory.getWritingTemporaryFilename("", 0);
        assertThat(tempName, is("19700101.000000.logdata.writing"));
        
        String finishedName = DataFileNameFactory.getFinishedFilename("", 0);
        assertThat(finishedName, is("19700101.000000.logdata"));
        
        assertThat(DataFileNameFactory.extractDatePart("",finishedName), is("19700101.000000"));
        assertThat(DataFileNameFactory.extractDatePart("",tempName), is("19700101.000000"));

    }
}
