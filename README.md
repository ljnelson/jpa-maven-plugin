jpa-maven-plugin
================

[![Jitpack Snapshots](https://jitpack.io/v/iSnow/jpa-maven-plugin.svg)](https://jitpack.io/#iSnow/jpa-maven-plugin)

This project houses a [Maven plugin][1] for auto-discovering JAP-annotated entity classes from libraries. The
primary use-case is developers who factor out model classes into a library and need JPA providers like Eclipselink
to see them. Spring developers do not need this plugin, neither do Hibernate developers.

If you use this from IntelliJ, you need to add a Maven goal `mvn package` to the "before launch" section
of your run configuration.

## Quick Start

## Compile and install the plugin

Sorry about that, but the plugin is not on central. To build and use it locally, type `mvn install` in the terminal.
The plugin will be installed into your .m2/repository and be available for local builds. CI pipelines will not like this.

## Use in projects

You should have a `persistence.xml` to be used for in
`src/main/resources/META-INF`:

    <?xml version="1.0" encoding="UTF-8"?>
    <persistence version="2.0"
                 xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
      <persistence-unit name="hibernate" transaction-type="RESOURCE_LOCAL">

        <!-- This provider is just an example. -->
        <provider>org.hibernate.ejb.HibernatePersistence</provider>

        <!-- This is where the magic will happen. -->
        <class>${entityClassnames}</class>

        <properties>
          <property name="javax.persistence.jdbc.driver" value="TODO: JDBC driver class name goes here" />
          <property name="javax.persistence.jdbc.url" value="TODO: JDBC connection URL goes here" />
          <property name="javax.persistence.jdbc.user" value="TODO: database user goes here" />
          <property name="javax.persistence.jdbc.password" value="TODO: database password goes here" />

          <!-- You may of course put any properties you want here -->
          <property name="hibernate.default_schema" value="TODO: default schema goes here" />
          <property name="hibernate.id.new_generator_mappings" value="true"/>
          <property name="hibernate.show_sql" value="true" />
          <property name="hibernate.format_sql" value="true" />
        </properties>
      </persistence-unit>
    </persistence>

Ensure that it does _not_ get copied during resource copying.  Add
this in your `pom.xml`'s `<build>` section:

    <resources>
      <resources>
        <directory>src/resources</directory>
        <excludes>
          <exclude>META-INF/persistence.xml</exclude>
        </excludes>
        <!-- Whether you want to filter the other resources is up to you -->
        <filtering>true</filtering>
      </resources>
    </resources>

Now set up the `jpa-maven-plugin`.  Place this in your `pom.xml`'s
`<build>`'s `<plugins>` section:

    <plugin>
      <groupId>com.edugility</groupId>
      <artifactId>jpa-maven-plugin</artifactId>
      <version>4-SNAPSHOT</version>
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
          <phase>process-classes</phase>
          <goals>
            <goal>copy-resources</goal>
          </goals>
          <configuration>
            <filters>
              <filter>${project.build.directory}/generated-sources/jpa-maven-plugin/entityClassnames.properties</filter>
            </filters>
            <outputDirectory>${project.build.outputDirectory}/META-INF</outputDirectory>
            <resources>
              <resource>
                <filtering>true</filtering>
                <directory>src/main/resources/META-INF</directory>
                <includes>
                  <include>persistence.xml</include>
                </includes>
              </resource>
            </resources>
          </configuration>
        </execution>
      </executions>
    </plugin>

Run `mvn clean process-classes` if you just want to see the
effects of the `jpa-maven-plugin` or `mvn clean package` to use it in your build.  Look in your
`target/classes/META-INF` directory.  You will see a
`persistence.xml` file with all of your entity and mapped superclass
and embeddables and id classes listed.  Any Maven lifecycle phases
that occur before `process-classes` will not be able to use the
effects of the `jpa-maven-plugin`.

## More Information

For more information, grab the source to this project, and from the root directory run:

    mvn clean install site

Full documentation will be available at `target/site/index.html`.

[1]: http://maven.apache.org/guides/plugin/guide-java-plugin-development.html
