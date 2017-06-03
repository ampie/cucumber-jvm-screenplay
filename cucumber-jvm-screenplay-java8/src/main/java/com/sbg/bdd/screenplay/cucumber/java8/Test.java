package com.sbg.bdd.screenplay.cucumber.java8;

import com.sbg.bdd.screenplay.core.util.OuterClass;

import java.util.function.Consumer;

public class Test {
    public static void main(String[] args) {
        Consumer<String> c = s -> {};
        System.out.println(OuterClass.of(c.getClass()));
    }
}
