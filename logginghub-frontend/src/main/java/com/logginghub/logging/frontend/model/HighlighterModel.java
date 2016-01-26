package com.logginghub.logging.frontend.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

public class HighlighterModel extends Observable {

    private ObservableProperty<String> colourHex = createStringProperty("colourHex", "");
    private ObservableProperty<String> phrase = createStringProperty("phrase", "");

    public HighlighterModel() {
        getColourHex().set("no colour hex");
        getPhrase().set("no phrase");
    }

    public ObservableProperty<String> getColourHex() {
        return colourHex;
    }

    public ObservableProperty<String> getPhrase() {
        return phrase;
    }
}
