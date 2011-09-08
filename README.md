jpa-maven-plugin
================

This project houses a [Maven plugin][1] for performing various tasks
to help with JPA-based projects.

In these early days the only mojo in here is the
`ListEntityClassnamesMojo`.  It will help you with unit and
integration testing.

The `ListEntityClassnamesMojo` creates a properties file that can then
be used as a Maven [filter][2] during unit testing (or for any other
purpose).  It scans the test classpath (by default) looking for Java
classes that have been annotated with either
`javax.persistence.Entity`, `javax.persistence.MappedSuperclass` or
`javax.persistence.IdClass`, and places their classnames in a
properties file under a configurable property key (or several).  The
classnames themselves may be prefixed and suffixed.

The net effect is that you can do this inside your `persistence.xml`:

    <class>${entityClasses}</class>
    
...and if you've [set up your Maven filtering properly][2], you'll get
something like this at test time:

    <class>com.foobar.FirstEntity</class>
    <class>com.foobar.SecondEntity</class>
    <class>com.foobar.dependency.SomeEntityFromADependencyJar</class>

[1]: http://maven.apache.org/guides/plugin/guide-java-plugin-development.html
[2]: http://maven.apache.org/plugins/maven-resources-plugin/copy-resources-mojo.html#filters
