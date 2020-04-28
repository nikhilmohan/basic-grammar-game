package com.learninggrammer.basicgrammergame.domain;

public enum GrammarType {
    GROUPING_WORDS("grouping_words"),
    PLURALS("plurals"),
    SYNONYMS("synonyms"),
    HAVE_HAS("have_has"),
    IS_AM_ARE("is_am_are"),
    SIMILE("simile"),
    ARTICLE("article"),
    ADVERB("adverb"),
    PREPOSITION("preposition"),
    SINGLE_WORD("single_word"),
    CONJUNCITON("conjunction");

    private String group;

    GrammarType(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
