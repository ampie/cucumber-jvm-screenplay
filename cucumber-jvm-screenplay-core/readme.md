#Cucumber JVM Screenplay
The Cucumber JVM Screenplay project offers basic support for the Screenplay pattern that is currently
available on Serenity and Serenity JS.
##What is the Screenplay pattern
The ScreenPlay pattern emerged as a useful way to structure test code to be both modular and self
describing. Two excellent implementations exist under the Serenity brand for both Java and JavaScript.
##Why for Cucumber (again)?
The ScreenPlay pattern has proven to be so useful, that we needed it on many different projects. It
 is true that Serenity already supports both ScreenPlay and Cucumber. Serenity is an excellent 
 product, and this is by no means an attempt to compete with it. Unfortunately Serenity does 
 bring in a lot of transitive dependencies which renders it incompatible with Android 
 and Espresso. Since we are planning implementations for other languages too (Swift is next),
 we decided to rather align this implementation of Screenplay with Cucumber, which is already
 a successful polyglot project.
##Screenplay beyond Cucumber
 It is important to keep in mind that the idea behind the Screenplay pattern is to support BDD type
 scenarios, even in the absence of a Gherkin implementation. Serenity and Serenity JS can work
 just as well outside of Cucumber, as it integrates with other testing tools. However, we have found
 that for a team on the BDD journey, it takes a while to wean them off the idea of having some kind
 of Gherkin file that they can look at. In these cases, Cucumber JVM Screenplay was a necessary
 stepping stone on the BDD journey. And even if you keep on using Cucumber, Screenplay does make
 for step definitions that even non-technical people can understand.
##I Screenplayed, now what?
 Cucumber JVM Screenplay wasn't developed in isolation. Reporting was limited to JSON specifically
 to allow better report generators such as Serenity to generate beautiful living documentation. 
 Please look at our new Cucumber JSON adaptor for Serenity. Consider using our Serenity FilterChain
 to consolidate reports generated from multiple sources
 