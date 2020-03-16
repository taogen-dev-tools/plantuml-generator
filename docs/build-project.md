# Build Project Document

- Environment
- Create Git Repository and Add gitignore
- Create and Configuration Maven Project
- Add Logging configuration File

## Environment

- JDK 8
- Maven
- Git
- Intellij IDEA

## Create Git Repository and Add gitignore



## Create and Configuration Maven Project

Creating Maven project by command line

```shell
mvn archetype:generate -DgroupId=com.taogen.docs2uml -DartifactId=apidocs2plantuml -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Edit Maven Configuration `pom.xml`

Dependencies

- log4j v2.8.2
- junit v4.12
- jsoup v1.12.1
- json-path v2.4.0
- lombok v1.18.4

Plugins

- maven-compiler-plugin
- maven-jar-plugin

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.source>1.8</maven.compiler.source>
	<!-- custom properties -->
    <project.java.version>1.8</project.java.version>
    <junit.version>4.12</junit.version>
    <log4j.version>2.8.2</log4j.version>
    <mvn.jar.plugin.mainclass>com.taogen.docs2uml.App</mvn.jar.plugin.mainclass>
</properties>
<dependencies>
    <!-- ** Unit Test ** -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>

    <!-- ** Logging ** -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-web</artifactId>
        <version>${log4j.version}</version>
    </dependency>
    
    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.4</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- crawler & parser -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.12.1</version>
    </dependency>
    <dependency>
        <groupId>com.jayway.jsonpath</groupId>
        <artifactId>json-path</artifactId>
        <version>2.4.0</version>
    </dependency>
    
</dependencies>
<build>
    <sourceDirectory>src/main/java</sourceDirectory>
    <testSourceDirectory>src/test/java</testSourceDirectory>
    <plugins>
        <!-- maven compile plugin -->
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.5.1</version>
            <configuration>
                <source>${project.java.version}</source>
                <target>${project.java.version}</target>
            </configuration>
        </plugin>
        
        <!-- maven package jar plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>${mvn.jar.plugin.mainclass}</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Add Logging configuration File

`src/main/resources/log4j2.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="com.taogen.docs2uml" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Root level="error">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

