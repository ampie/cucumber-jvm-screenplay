package com.sbg.bdd.screenplay.core.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Utility class to convert test case and method names into human-readable form.
 *
 */
public final class NameConverter {

    private static final String INDEXED_METHOD_NAME = ".*\\[\\d+]";
    private static final String[] abbreviations = {"CSV", "XML", "JSON"};

    private NameConverter() {
    }
    public static String decapitalize(String name){
        if (name == null || name.length() == 0) {
            return "";
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);

    }
    /**
     * Converts a class or method name into a human-readable sentence.
     *
     * @param name a class or method name
     * @return the human-readable form
     */
    public static String humanize(final String name) {

        if ((name == null) || (name.trim().length() == 0)) {
            return "";
        }
        if (name.contains(" ") && !thereAreParametersIn(name)) {
            return name;
        } else if (thereAreParametersIn(name)) {
            return humanizeNameWithParameters(name);
        } else {

            String noUnderscores = name.replaceAll("_", " ");
            String splitCamelCase = splitCamelCase(noUnderscores);

            Set<Acronym> acronyms = Acronym.acronymsIn(splitCamelCase);
            String capitalized = StringUtils.capitalize(splitCamelCase);
            for (Acronym acronym : acronyms) {
                capitalized = acronym.restoreIn(capitalized);
            }
            return restoreAbbreviations(capitalized);
        }
    }

    private static String restoreAbbreviations(final String sentence) {
        String processing = sentence;
        for (String abbreviation : abbreviations) {
            processing = processing.replaceAll(StringUtils.capitalize(abbreviation), abbreviation);
        }
        return processing;
    }

    private static String humanizeNameWithParameters(final String name) {
        int parametersStartAt = name.indexOf(": ");
        String bareName = name.substring(0, parametersStartAt);
        String humanizedBareName = humanize(bareName);
        String parameters = name.substring(parametersStartAt);
        return humanizedBareName + parameters;
    }

    private static boolean thereAreParametersIn(final String name) {
        return name.contains(": ");
    }

    /**
     * Inserts spaces between words in a CamelCase name.
     *
     * @param name a name in camel-case
     * @return the name with spaces instead of underscores
     */
    public static String splitCamelCase(final String name) {
        List<String> splitWords = new ArrayList<>();

        String[] phrases = name.split("\\s");

        for (String phrase : phrases) {
            splitWords.addAll(splitWordsIn(phrase));
        }

        String splitPhrase =  StringUtils.join(splitWords, " ");
        return splitPhrase.trim();
    }

    private static List<String> splitWordsIn(String phrase) {

        List<String> splitWords = new ArrayList<>();

        String currentWord = "";
        for (int index = 0; index < phrase.length(); index++) {
            if (onWordBoundary(phrase, index)) {
                splitWords.add(lowercaseOrAcronym(currentWord));
                currentWord = String.valueOf(phrase.charAt(index));
            } else {
                currentWord = currentWord + (phrase.charAt(index));
            }
        }
        splitWords.add(lowercaseOrAcronym(currentWord));

        return splitWords;
    }

    private static String lowercaseOrAcronym(String word) {
        if (Acronym.isAnAcronym(word)) {
            return word;
        } else {
            return StringUtils.lowerCase(word);
        }
    }

    private static boolean onWordBoundary(String name, int index) {
        return (uppercaseLetterAt(name, index)
                && (lowercaseLetterAt(name, index - 1) || lowercaseLetterAt(name, index + 1)));
    }

    private static boolean uppercaseLetterAt(String name, int index) {
        return CharUtils.isAsciiAlphaUpper(name.charAt(index));
    }

    private static boolean lowercaseLetterAt(String name, int index) {
        return (index >= 0)
                && (index < name.length())
                && CharUtils.isAsciiAlphaLower(name.charAt(index));
    }


    private final static Map<Character, String> EXCLUDE_FROM_FILENAMES = new HashMap<>();

    static {
        EXCLUDE_FROM_FILENAMES.put('$', "_");
        EXCLUDE_FROM_FILENAMES.put('/', "_");
        EXCLUDE_FROM_FILENAMES.put('\\', "_");
        EXCLUDE_FROM_FILENAMES.put(':', "_");
        EXCLUDE_FROM_FILENAMES.put(';', "_");
        EXCLUDE_FROM_FILENAMES.put('<', "_lt_");
        EXCLUDE_FROM_FILENAMES.put('>', "_gt_");
        EXCLUDE_FROM_FILENAMES.put('[', "_obr_");
        EXCLUDE_FROM_FILENAMES.put(']', "_cbr_");
        EXCLUDE_FROM_FILENAMES.put('{', "_obrc_");
        EXCLUDE_FROM_FILENAMES.put('}', "_cbrc_");
        EXCLUDE_FROM_FILENAMES.put('*', "_star_");
        EXCLUDE_FROM_FILENAMES.put('^', "_caret_");
        EXCLUDE_FROM_FILENAMES.put('%', "_per_");
        EXCLUDE_FROM_FILENAMES.put('"', "_quote_");
        EXCLUDE_FROM_FILENAMES.put('?', "_question_");
        EXCLUDE_FROM_FILENAMES.put('|', "_pipe_");
        EXCLUDE_FROM_FILENAMES.put('&', "_amp_");
        EXCLUDE_FROM_FILENAMES.put(',', "_comma_");
        EXCLUDE_FROM_FILENAMES.put('=', "_equals_");
        EXCLUDE_FROM_FILENAMES.put('\'', "_");
        EXCLUDE_FROM_FILENAMES.put('\"', "_");
        EXCLUDE_FROM_FILENAMES.put('@', "_at_");
        EXCLUDE_FROM_FILENAMES.put('#', "_hash_");
        EXCLUDE_FROM_FILENAMES.put('+', "_plus_");
        EXCLUDE_FROM_FILENAMES.put(' ', "_");
    }

    public static String filesystemSafe(final String name) {
        if (name == null) {
            return name;
        }

        String safeName = name.trim();
        for (Character substitutableChar : EXCLUDE_FROM_FILENAMES.keySet()) {
            safeName = StringUtils.replace(safeName, substitutableChar.toString(), EXCLUDE_FROM_FILENAMES.get(substitutableChar));
        }
        return safeName;
    }

}
