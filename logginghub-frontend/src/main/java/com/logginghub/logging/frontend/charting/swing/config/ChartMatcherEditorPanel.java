package com.logginghub.logging.frontend.charting.swing.config;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.utils.observable.Binder;

public class ChartMatcherEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel legend;
	private JLabel pattern;

	public ChartMatcherEditorPanel() {
		setLayout(new MigLayout("fill", "" ,""));
		
		JLabel patternLabel = new JLabel("Pattern");
		JLabel legendLabel = new JLabel("Legend");
		
		pattern = new JLabel("");
		legend = new JLabel("");
		
		patternLabel.setLabelFor(pattern);
		legendLabel.setLabelFor(legend);
		
		add(patternLabel);
		add(pattern);
		add(legendLabel);
		add(legend);
	}

	public void bind(ChartSeriesModel model) {
//		Binder.bind(model.getLegend(), legend);
		Binder.bind(model.getPatternID(), pattern);
	}
	
}
