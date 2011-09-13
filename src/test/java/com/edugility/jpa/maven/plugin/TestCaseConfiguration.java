/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * $Id$
 *
 * Copyright (c) 2010-2011 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.jpa.maven.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import org.apache.maven.project.MavenProject;

import org.apache.maven.model.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * An {@link AbstractMojoTestCase} that ensures that configuration
 * values as interpreted by Maven actually call the methods on an
 * {@link ListEntityClassnamesMojo} instance as they should.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 */
public class TestCaseConfiguration extends AbstractMojoTestCase {

  /**
   * The {@link ListEntityClassnamesMojo} under test.  This field must
   * never be {@code null} during a test run.
   *
   * @see #setUp()
   */
  protected ListEntityClassnamesMojo mojo;

  /**
   * Loads up {@code
   * ${project.build.testOutputDirectory}/test-project/pom.xml}, tells
   * the Maven plugin testing harness to read it, and then gets the
   * configured object from the harness and installs it as the value
   * of the {@link #mojo} field.
   *
   * @exception Exception if an error occurs
   *
   * @see #mojo
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    final File testPom = 
      new File(this.getTestOutputDirectory(), String.format("test-project%spom.xml", File.separator));
    assertTrue(testPom.isFile());
    assertTrue(testPom.canRead());
    this.mojo = new ListEntityClassnamesMojo();
    this.configureMojo(this.mojo, "jpa-maven-plugin", testPom);
  }

  /**
   * Returns the best available value for the writable, transient
   * directory where the current build is happening.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the build directory, never {@code null}
   */
  public final File getBuildDirectory() {
    return 
      new File(System.getProperty("maven.project.build.directory",
                                  System.getProperty("project.build.directory",
                                                     String.format("%1$s%2$starget",
                                                                   getBasedir(), File.separator))));
  }

  /**
   * Returns the best available value for the writable, transient
   * directory where test classes are compiled to.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the test output directory, never {@code null}
   */
  public File getTestOutputDirectory() {
    return
      new File(System.getProperty("maven.project.build.testOutputDirectory",
                                  System.getProperty("project.build.testOutputDirectory",
                                                     String.format("%1$s%2$starget%2$stest-classes",
                                                                   getBasedir(), File.separator))));
  }

  /**
   * Ensures that Maven's configuration of the {@link #mojo} field's
   * contents actually worked.
   *
   * @exception Exception if an error occurred
   */
  public void testConfigurationWorked() throws Exception {
    final MavenProject project = this.mojo.getProject();
    assertNotNull(project);
    final Build build = project.getBuild();
    assertNotNull(build);
    assertNotNull(build.getTestOutputDirectory());
    final File mojoTestOutputDirectory = new File(build.getTestOutputDirectory());
    assertEquals(this.getTestOutputDirectory(), mojoTestOutputDirectory);

    final List<?> testClasspathElements = project.getTestClasspathElements();
    assertNotNull(testClasspathElements);
    assertEquals(1, testClasspathElements.size());
    final String element = testClasspathElements.get(0).toString();
    assertNotNull(element);
    assertEquals(build.getTestOutputDirectory(), element);
    
    assertEquals("<class>", this.mojo.getPrefix());

    // It would be nice to configure it for </class>\n, but the
    // newline preservation is not honored by whatever stub silliness
    // is going on in the plugin harness.  CDATA doesn't even work.
    // So we've just configured it without the newline.  This will
    // make for some ugly output, but that's it.
    assertEquals("</class>", this.mojo.getSuffix());

    final AnnotationDB db = this.mojo.cloneAnnotationDB();
    assertNotNull(db);
    final String[] ignoredPackages = db.getIgnoredPackages();
    assertNotNull(ignoredPackages);
    assertEquals(11, ignoredPackages.length);

    assertEquals("com.google", ignoredPackages[0]);
    assertEquals("com.sun", ignoredPackages[1]);
    assertEquals("java", ignoredPackages[2]);
    assertEquals("javax", ignoredPackages[3]);
    assertEquals("liquibase", ignoredPackages[4]);
    assertEquals("org.eclipse", ignoredPackages[5]);
    assertEquals("org.glassfish", ignoredPackages[6]);
    assertEquals("org.hamcrest", ignoredPackages[7]);
    assertEquals("org.hibernate", ignoredPackages[8]);
    assertEquals("org.jboss", ignoredPackages[9]);
    assertEquals("org.junit", ignoredPackages[10]);

    assertTrue(!db.getScanFieldAnnotations());
    assertTrue(!db.getScanMethodAnnotations());
    assertTrue(!db.getScanParameterAnnotations());
    assertTrue(db.getScanClassAnnotations());
  }

  /**
   * Runs the {@link ListEntityClassnamesMojo} goal on this project's
   * test classes and verifies that the output is correct.
   *
   * @exception Exception if an error occurs
   */
  public void testExecuteOnThisProjectsClasses() throws Exception {
    this.mojo.execute();
    final File propertiesFile = 
      new File(this.getBuildDirectory(), 
               String.format("generated-test-sources%1$sjpa-maven-plugin%1$sentityClassnames.properties",
                             File.separator));
    assertTrue(propertiesFile.canRead());
    assertTrue(propertiesFile.isFile());
    final Properties properties = new Properties();
    final FileReader reader = new FileReader(propertiesFile);
    properties.load(reader);
    reader.close();
    assertNotNull(properties.getProperty("edugilityClasses"));
  }

  /**
   * A {@link SystemStreamLog} that is enabled for debug logging.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0-SNAPSHOT
   *
   * @since 1.0-SNAPSHOT
   */
  public static final class SystemStreamLogWithDebugEnabled extends SystemStreamLog {
    
    /**
     * Overrides the default behavior of this method to return {@code
     * true} in all cases.
     *
     * @return {@code true}
     */
    @Override
    public boolean isDebugEnabled() {
      return true;
    }
    
  }

}