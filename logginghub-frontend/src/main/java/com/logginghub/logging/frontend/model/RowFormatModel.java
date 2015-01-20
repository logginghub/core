package com.logginghub.logging.frontend.model;

import java.awt.Color;
import java.awt.Font;

import com.logginghub.logging.frontend.configuration.RowFormatConfiguration;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.FontUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

public class RowFormatModel extends Observable {

    private ObservableProperty<Color> foregroundColour = new ObservableProperty<Color>(Color.black, this);
    private ObservableProperty<Color> backgroundColour = new ObservableProperty<Color>(Color.cyan, this);
    private ObservableProperty<Color> borderColour = new ObservableProperty<Color>(Color.black, this);
    private ObservableInteger borderWidth = new ObservableInteger(1, this);
    private ObservableProperty<Font> font = new ObservableProperty<Font>(null);

    public ObservableProperty<Color> getBackgroundColour() {
        return backgroundColour;
    }

    public ObservableProperty<Color> getBorderColour() {
        return borderColour;
    }

    public ObservableInteger getBorderWidth() {
        return borderWidth;
    }

    public ObservableProperty<Font> getFont() {
        return font;
    }

    public ObservableProperty<Color> getForegroundColour() {
        return foregroundColour;
    }

    public static RowFormatModel fromConfiguration(RowFormatConfiguration configuration) {
        RowFormatModel model = new RowFormatModel();
        model.getBackgroundColour().set(ColourUtils.parseColor(configuration.getBackgroundColour()));
        model.getBorderColour().set(ColourUtils.parseColor(configuration.getBorderColour()));
        model.getForegroundColour().set(ColourUtils.parseColor(configuration.getForegroundColour()));
        model.getBorderWidth().set(configuration.getBorderLineWidth());
        model.getFont().set(FontUtils.parseFont(configuration.getFont()));
        return model;
    }

}
