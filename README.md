jpa-maven-plugin
================

[![Jitpack Snapshots](https://jitpack.io/v/iSnow/jpa-maven-plugin.svg)](https://jitpack.io/#iSnow/jpa-maven-plugin)

This project houses a [Maven plugin][1] for performing various tasks
to help with JPA-based projects.

## Quick Start

Create a `persistence.xml` to be used for unit testing in
`src/test/resources/META-INF`:

    <?xml version="1.0" encoding="UTF-8"?>
    <persistence version="2.0"
                 xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
      <persistence-unit name="test-Hibernate" transaction-type="RESOURCE_LOCAL">

        <!-- This provider is just an example. -->
        <provider>org.hibernate.ejb.HibernatePersistence</provider>

        <!-- This is where the magic will happen. -->
        <class>${entityClassnames}</class>

        <properties>
          <property name="javax.persistence.jdbc.driver" value="TODO: JDBC driver class name goes here" />
          <property name="javax.persistence.jdbc.url" value="TODO: JDBC connection URL for unit tests goes here" />
          <property name="javax.persistence.jdbc.user" value="TODO: unit test database user goes here" />
          <property name="javax.persistence.jdbc.password" value="TODO: unit test database password goes here" />

          <!-- You may of course put any properties you want here -->
          <property name="hibernate.default_schema" value="TODO: default test schema goes here" />
          <property name="hibernate.id.new_generator_mappings" value="true"/>
          <property name="hibernate.show_sql" value="true" />
          <property name="hibernate.format_sql" value="true" />
        </properties>
      </persistence-unit>
    </persistence>

Ensure that it does _not_ get copied during resource copying.  Add
this in your `pom.xml`'s `<build>` section:

    <testResources>
      <testResource>
        <directory>src/test/resources</directory>
        <excludes>
          <exclude>META-INF/persistence.xml</exclude>
        </excludes>
        <!-- Whether you want to filter the other test resources is up to you -->
        <filtering>true</filtering>
      </testResource>
    </testResources>

Now set up the `jpa-maven-plugin`.  Place this in your `pom.xml`'s
`<build>`'s `<plugins>` section:

    <plugin>
      <groupId>com.edugility</groupId>
      <artifactId>jpa-maven-plugin</artifactId>
      <version>1.1-SNAPSHOT</version>
      <executions>
        <execution>
          <id>Generate entityClassnames.properties</id>
          <goals>
            <goal>list-entity-classnames</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

Finally, make sure that the `persistence.xml` is copied over, but only
after the `jpa-maven-plugin` has run.  Place this **IMMEDIATELY
BELOW** the plugin stanza listed above:

    <plugin>
      <artifactId>maven-resources-plugin</artifactId>
      <version>2.5</version>
      <executions>
        <execution>
          <id>Copy persistence.xml filtered with generated entityClassnames.properties file</id>
          <phase>process-test-classes</phase>
          <goals>
            <goal>copy-resources</goal>
          </goals>
          <configuration>
            <filters>
              <filter>${project.build.directory}/generated-test-sources/jpa-maven-plugin/entityClassnames.properties</filter>
            </filters>
            <outputDirectory>${project.build.testOutputDirectory}/META-INF</outputDirectory>
            <resources>
              <resource>
                <filtering>true</filtering>
                <directory>src/test/resources/META-INF</directory>
                <includes>
                  <include>persistence.xml</include>
                </includes>
              </resource>
            </resources>
          </configuration>
        </execution>
      </executions>
    </plugin>

Run `mvn clean process-test-classes` if you just want to see the
effects of the `jpa-maven-plugin`.  Look in your
`target/test-classes/META-INF` directory.  You will see a
`persistence.xml` file with all of your entity and mapped superclass
and embeddables and id classes listed.  Any Maven lifecycle phases
that occur before `process-test-classes` will not be able to use the
effects of the `jpa-maven-plugin`.

## More Information

For more information, grab the source to this project, and from the root directory run:

    mvn clean install site

Full documentation will be available at `target/site/index.html`.

[1]: http://maven.apache.org/guides/plugin/guide-java-plugin-development.html
