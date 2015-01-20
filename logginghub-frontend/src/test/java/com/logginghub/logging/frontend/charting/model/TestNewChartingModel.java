package com.logginghub.logging.frontend.charting.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.utils.ResourceUtils;


public class TestNewChartingModel {

    @Test public void test_matt_configuration_loads() {
        
        NewChartingModel model = new NewChartingModel();
        model.fromXml(ResourceUtils.read("charting-matt.xml"));
        
        assertThat(model.getPatternModels().size(), is(6));
        assertThat(model.getPatternModels().get(3).getName().get(), is("Erroneous"));

        assertThat(model.getPages().size(), is(2));
        assertThat(model.getPages().get(0).getName().get(), is("Long Term Charting"));
        assertThat(model.getPages().get(0).getChartingModels().size(), is(6));
        assertThat(model.getPages().get(0).getPieChartModels().size(), is(4));

        assertThat(model.getPages().get(1).getName().get(), is("Daily Charting"));
        assertThat(model.getPages().get(1).getChartingModels().size(), is(6));
        assertThat(model.getPages().get(1).getPieChartModels().size(), is(4));
        
        
    }
    
}
