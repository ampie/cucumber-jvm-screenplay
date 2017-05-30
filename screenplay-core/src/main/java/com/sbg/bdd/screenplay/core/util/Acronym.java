package com.sbg.bdd.screenplay.core.util;


import java.util.HashSet;
import java.util.Set;

class Acronym {
    private final String acronymText;
    private final int start;
    private final int end;

    Acronym(String acronym, int start, int end) {
        this.acronymText = acronym;
        this.start = start;
        this.end = end;
    }

    public static Set<Acronym> acronymsIn(String text) {
        Set<Acronym> acronyms = new HashSet<>();

        String[] words = text.split("\\W");
        for (String word : words) {
            if (isAnAcronym(word)) {
                acronyms.addAll(appearencesOf(word, text));
            }
        }
        return acronyms;
    }

    public String restoreIn(String text) {
        String prefix = (start > 0) ? text.substring(0, start) : "";
        String suffix = text.substring(end, text.length());
        return prefix + acronymText + suffix;
    }

    private static Set<Acronym> appearencesOf(String word, String text) {
        Set<Acronym> acronyms = new HashSet<>();

        int startAt = 0;
        while (startAt < text.length()) {
            int wordFoundAt = text.indexOf(word, startAt);
            if (wordFoundAt == -1) {
                break;
            }

            acronyms.add(new Acronym(word, wordFoundAt, wordFoundAt + word.length()));
            startAt = wordFoundAt + word.length();
        }
        return acronyms;
    }

    public static boolean isAnAcronym(String word) {
        return (word.length() > 1) && Character.isUpperCase(firstLetterIn(word)) && Character.isUpperCase(lastLetterIn(word));
    }

    private static char firstLetterIn(String word) {
        return word.toCharArray()[0];
    }

    private static char lastLetterIn(String word) {
        return word.toCharArray()[word.length() - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Acronym acronym = (Acronym) o;
        return start == acronym.start &&
                end == acronym.end &&
                acronymText.equals(acronym.acronymText);
    }

    @Override
    public int hashCode() {
        return start;
    }
}