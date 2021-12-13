# UdaSecurity
Refactor the existing home security application into multiple modules, unit-testable, and fix all the bugs:
- Using Maven to create a new project structure. The standard directory layout allows Maven to automatically looks for source code, resources, tests, and other files in certain folders by default.
- Importing all the required dependencies to make sure the application is executable.
- Splitting all the java classes into two modules so that the code is more organized:
  - Image Service
  - Security Service
- Writting Unit Test for the SecurityService class. Using the Mock annotation in Mockito dependency to help subsititute denpendencies and keep the scope of each unit test narrow.
- Fixing all the bugs based on the result of the Unit Test.
- The Unit Test coverage of SecurityService class is 100%. That means all the methods in the class are tested.
- Compiling the application into an executable JAR file so that the application can be started from the command line.
- Using spotbugs Maven plugin to generate a spotbugs.html.
