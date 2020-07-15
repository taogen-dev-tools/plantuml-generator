# plantuml-generator
[![GitHub stars](https://img.shields.io/github/stars/tagnja/plantuml-generator)](https://github.com/tagnja/plantuml-generator/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/tagnja/plantuml-generator)](https://github.com/tagnja/plantuml-generator/network/members)
[![GitHub issues](https://img.shields.io/github/issues/tagnja/plantuml-generator)](https://github.com/tagnja/plantuml-generator/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/tagnja/plantuml-generator)](https://github.com/tagnja/plantuml-generator/pulls)
[![Build Status](https://travis-ci.com/tagnja/plantuml-generator.svg?branch=master)](https://travis-ci.com/tagnja/plantuml-generator)

## What's plantuml-generator

plantuml-generator is a tool for generating UML diagrams (that mainly is class diagrams). The plantuml-generator helps you automate generating PlantUML text by a Javadoc API website URL. It fetches [Javadoc](https://en.wikipedia.org/wiki/Javadoc#:~:text=Javadoc%20(originally%20cased%20JavaDoc)%20is,format%20from%20Java%20source%20code.) HTML pages of any techniques API such as [Java SE](https://docs.oracle.com/javase/8/docs/api/) and [Spring framework](https://docs.spring.io/spring-framework/docs/current/javadoc-api/), then parsing HTML pages to a [PlantUML](https://plantuml.com/) text.

## How to develop

To develop plantuml-generator, you need to install the following software:

- Git
- JDK 8
- Maven

You can choose any Java IDE to develop this project, We strongly suggest you use the [IntelliJ IDEA](https://www.jetbrains.com/idea/).

## How to run

### To get this project executable jar file

First of all, you need to get the `plantuml-generator.jar` executable file of this project. 

You can get the executable jar file by using Maven command to package the source code. The target packaged jar file is `plantuml-generator-jar-with-dependencies.jar`, and you can rename to `plantuml-generator.jar` or others.

```shell
# clone this project to your local
git clone https://github.com/tagnja/plantuml-generator.git
# package source code to jar
mvn package -Dmaven.test.skip=true
# rename jar file
mv target/plantuml-generator-jar-with-dependencies.jar target/plantuml-generator.jar
```

You can also get the executable jar file by downloading from [GitHub releases](https://github.com/tagnja/plantuml-generator/releases).

### Running this project to generate plantUML text

You can run the following command to get the generated PlantUML text. 

```java
java -jar plantuml-generator.jar -u <javadoc_url> -p <package_path> 
```

For example, 

```java
java -jar plantuml-generator.jar -u https://docs.oracle.com/javase/8/docs/api/ -p java.nio
```

After running this jar file, you will start the project, and get the result of generating PlantUML text. More details about this command refer to "Usage" section of this README.

### Using the plantuml.jar to generate UML diagrams

You can download the `plantuml.jar` from [PlantUML](https://plantuml.com/download).

Running the following command to get the UML diagram.

```shell
java -jar plantuml.jar <plantuml_text_path>
```

More detail about PlantUML usage refer to [PlantUML](https://plantuml.com/download).

There are some common problems for using the `plantuml.jar`:

- If the generating picture can't contain all classes information, you need to set generating configuration to increase the size of picture. The configuration is `-DPLANTUML_LIMIT_SIZE=<your_specified_size>`. For example, 

  ```
  java -DPLANTUML_LIMIT_SIZE=8192 -jar plantuml.jar plantuml_text.txt
  ```

- If you can't use plantuml.jar to generating diagram. Ensure you installed [JDK](https://www.java.com/en/download/) and [Graphviz](https://plantuml.com/graphviz-dot).

  

## Usage

| Option             | Description                                                  | Default | Required | Examples                                                     |
| ------------------ | ------------------------------------------------------------ | ------- | -------- | ------------------------------------------------------------ |
| -u or --url        | The Javadoc website URL.                                     | null    | Yes      | -u https://docs.oracle.com/javase/8/docs/api/, or -u http://localhost/java-docs |
| -p or --package    | The package path you want to generate.                       | null    | Yes      | -p java.nio                                                  |
| -s or --subpackage | Whether fetch the subpackage.                                | false   | No       | -s true                                                      |
| -m or --members    | Whether contains classes' members.                           | true    | No       | -m false                                                     |
| -c or --class      | Only generating parent classes and subclasses of a specified class. | null    | No       | -c Buffer                                                    |

Note: We strongly suggest to download Javadoc that you need and to deploy the Javadoc in your local PC. It's faster, and be more friendly to other websites. 

