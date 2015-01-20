package com.logginghub.logging.frontend.model;

public class HighlighterModel extends ObservableModel {

    public enum Fields implements FieldEnumeration {
        ColourHex,
        Phrase;
    }

    public HighlighterModel() {
        set(Fields.ColourHex, "no colour hex");
        set(Fields.Phrase, "no phrase");
    }
}
