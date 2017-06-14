package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.annotations.Scene;
import org.junit.Test;

public class ATest {
    @Test()
    @Scene("John sends the request successfullly")
    public void testIt() {
//        Actor john = actorNamed("John");
//        givenThat(john).wasAbleTo(
//                get("http://localhost:8080", with().queryParam("hello", "world"))
//        );
//        when(john).attemptsTo(
//                put("http://localhost:8080", with().body("hello"))
//        );
//        theLastResponse().assertThat().body("ASdf", hasItems(1, 2));
//        thenFor(john,
//                assertThat().body("asdfasdf", hasItems(1, 2, 3)),
//                assertThat().header("name", is(equalTo("1")))
//        );
//        then(john).should(seeThat(bodyAs(String.class), is("asdf")));
    }
}
