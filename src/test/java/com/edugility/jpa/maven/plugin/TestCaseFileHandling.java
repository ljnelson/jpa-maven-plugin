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
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

 /**
  * A <a href="http://www.junit.org/">JUnit</a> test case that
  * exercises the file handling portion of the {@link
  * ListEntityClassnamesMojo}.
  *
  * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
  *
  * @version 1.0-SNAPSHOT
  *
  * @since 1.0-SNAPSHOT
  */
public class TestCaseFileHandling {

  /**
   * A {@link File} representation of the return value of {@linkplain
   * System#getProperty(String) System.getProperty("java.io.tmpdir")}.
   */
  private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

  /**
   * A stupidly-named directory that we are 99.9% sure does not exist.
   * This field is never {@code null} during a test run.
   *
   * @see #setUp()
   */
  private File bargle;

  /**
   * The {@link ListEntityClassnamesMojo} under test.  This field must
   * never be {@code null} during a test run.
   *
   * @see #setUp()
   */
  protected ListEntityClassnamesMojo mojo;

  /**
   * Runs before each test and sets up a private temporary directory
   * and the contents of the {@link #mojo} field.
   *
   * @see Before
   */
  @Before
  public void setUp() {
    this.bargle =  new File(tmpDir, "bargle");
    this.bargle.deleteOnExit();
    assertTrue(!this.bargle.exists());

    this.mojo = new ListEntityClassnamesMojo();
  }

  /**
   * Runs after each test and ensures that all temporary resources
   * that were created are deleted.
   *
   * @see #setUp()
   *
   * @see After
   */
  @After
  public void tearDown() {
    if (this.bargle.exists()) {
      assertTrue(this.bargle.isDirectory());
      final File[] files = this.bargle.listFiles();
      assertNotNull(files);
      for (final File f : files) {
        f.delete();
        f.deleteOnExit();
      }
      this.bargle.delete();
    }
  }

  /**
   * Ensures that file handling is robust when the supplied file is
   * non-existent.
   *
   * @exception Exception if an error occurs
   */
  @Test
  public void testInitializeOutputFileWithNonExistentFile() throws Exception {
    final File boozle = new File(this.bargle, "boozle");
    boozle.deleteOnExit();
    assertTrue(!boozle.exists());

    final File outputFile = this.mojo.initializeOutputFile(boozle);
    assertNotNull(outputFile);
    outputFile.deleteOnExit();

    // Look, ma, this.bargle now exists
    assertTrue(this.bargle.isDirectory());
    assertTrue(this.bargle.canWrite());

    assertTrue(!boozle.exists());
  }

  /**
   * Ensures that file handling is robust when the supplied file
   * exists.
   *
   * @exception Exception if an error occurs
   */
  @Test
  public void testInitializeOutputFileWithExistentFile() throws Exception {
    File file = new File(this.bargle, "outputFile");
    file.deleteOnExit();
    assertTrue(!file.exists());
    assertTrue(file.getParentFile().mkdirs());
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.canWrite());
    file = this.mojo.initializeOutputFile(file);
    assertTrue(file.isFile());
    assertTrue(file.canWrite());
    file.delete();
  }

  /**
   * Ensures that file handling is robust when the supplied file is
   * relative and non-existent.
   *
   * @exception Exception if an error occurs
   */
  @Test
  public void testInitializeOutputFileWithRelativeNonExistentFile() throws Exception {
    final File boozle = new File("boozle");
    boozle.deleteOnExit();
    assertTrue(!boozle.exists());
    
    final String buildDirectoryName = this.mojo.getProjectBuildDirectoryName();
    assertNotNull(buildDirectoryName);
    final File buildDirectory = new File(buildDirectoryName);
    assertTrue(buildDirectory.isDirectory());
    assertTrue(buildDirectory.canWrite());

    final File outputFile = this.mojo.initializeOutputFile(boozle);
    assertNotNull(outputFile);
    outputFile.deleteOnExit();
    assertEquals(buildDirectory.getPath() + File.separator + "generated-test-sources" + File.separator + "jpa-maven-plugin", outputFile.getParent());
  }

}