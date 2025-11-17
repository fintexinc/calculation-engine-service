<!-- ABOUT THE PROJECT -->
## Portfolio Calculation Service

<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

* Git
* Maven 3.6.2+
* [JDK 17](https://adoptopenjdk.net/?variant=openjdk17)

### AD Groups

### Installation
To get a local copy up and running follow these simple example steps.
* Clone the project




#### Intellij Idea
Set environment variables in Intellij in the Edit Configuration dialog:
* Go to Edit configuration
* Modify options -> Enable Environment variables

#### Build and run with Maven
* from command line using Maven:

  ```bash
    $ mvn clean install -DskipTests=true
  ``` 
  ```bash
    $ mvn spring-boot:run -D"spring-boot.run.profiles"=localdev
  ```

* from IDEA:





```bash
$ mvn test
```