#Cucumber JVM Scoping
The goal of Cucumber JVM Scoping is to offer step definition implementors a consistent, strucutured approach
 to store state and provide a more fine grained mechanism for hooks (callbacks) to manipulate the state.
  
##Background
  State management is one of the more tricky aspects of automated tests. Purists believe tests should
  be absolutely stateless. 
###Nirvana  
  The benefits of stateless tests are quite clear to see:
  1. Each test can execute in isolation.
  2. Tests don't afffect each other's state, which eliminates possible contimaniation from other test cases.
  3. Each test is absolutely self contained, thus easy to maintain
  4. Isolated tests encourage developers to 'zoom' into the functionality under test which improves modularity.  
  So if you can keep your tests absolutely stateless, please do so. It is defeinitely possible to do so when 
  using state of the art domain driven development techniques and libraries.
###Reality bites  
  However, most of us are not so privileged. We almost always have to integrate with legacy software or code,
  , and often often it wasn't developed through TDD. Expect such software to have limited support for automated
  test data provisioning. Their interfaces often do not support any for of idempotency. And sometimes the legacy
  software is just plain slow.
  There can be challenges even on the software that we ourselves maintain. Sometimes an overall design is dictated
  to us that simply does not support the level of modularity required for isolated tests. Sometimes our fellow
  developers simply didn't have the skill, discipline or inclination to enforce such modularity.
  Let's be honest, the nirvana of highly modular software with highly isolated, blistering fast test cases is 
  much less common than we would like.
###What we need
  In these cases, in order to keep test execution time to a minimum, we often have the following needs:
  1. Test data provisioning is expensive, and we would like to limit repetitive provisioning and make the 
   test data available beyond a single scenario.
  2. Stubbing out downstream software can be complex, and we would like certain repeated stubs to be applicable to
  more than a single scenario.
  3. Where general test setup is expensive, we may want to limit the number of times we perform such setup.
  
  However, through all of this, we still need tests to execute in isolation. We may want to share state across
  tests, but we still need guarrantees that certain data will not be contimanated by other tests. And we want
  to avoid the spaghetti code that results from a massive global store of state at all costs 
  
##Enter Cucumber JVM Scoping
  Cucumber JVM Scoping offers a hierarchically scoped test run that punctuates a simple lifecycle for each scope.
  Developers can subscribe callbacks to significant lifecycle events, where they can associated specific state 
  with the scope in question. Accessing previously stored state from the scope intuitively follows the normal
  rules of programming languages. Variables are resolved from the innermost scope first, and when not found, 
  scopes are queried in an outwards direction until the variable is found.

##Screenplay support
  The Screenplay pattern is a new technique used for behaviour driven development, where typical test 
  operations are always performed within the context of a user. Cucumber JVM Scoping supports this paradigm,
  but in addition to the traditional concept of an Actor, it also introduced two other user contexts: 
  the Everybody and Guest contexts.
     
###Everybody
   This is the context where rules and state applicable to everybody (or perhaps sometimes anybody) can be managed
   from. When retrieving state from a specific scope, the Everybody context is queried by default. When retrieving
   state from a specific user context, when no matching variable is found, the Everybody context at the
   same level of scope is queried first. The idea is also that stubs, expectations and verifications that
   are defined for this context, to be defined such a way that it is applicable to everybody or anybody. 
###Guest
   This is basically the context of the unauthenticated user. Typically, this user would not have any data in the
   systen under test.
    
###The PersonaClient
   The PersonaClient interface can be implemented for systems that involve user profiles. When using the Screenplay
   pattern, it is common for most of the test data to be associated in some way with the current Actor. In fact, a
   common teqnique in BDD is to try to explore different contexts at the hand of well defined Persona's that are
   expected to have very specific data associated with them.  
     
   For such  cases, it is highly recommended to implement a 'PersonaService' that provides basic CRUD operations for the
   user profile. The PersonaClient can then be implemented as a reusable entrypoint into the PersonaService.
