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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A <a href="http://junit.org/">JUnit</a> test case to test the fix
 * for <a
 * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 */
public class TestCaseBugWorkaround {

  /**
   * The {@link ListEntityClassnamesMojo} under test.  This field must
   * never be {@code null} during a test run.
   *
   * @see #setUp()
   */
  protected ListEntityClassnamesMojo mojo;

  /**
   * Creates a new {@link TestCaseBugWorkaround} instance.
   */
  public TestCaseBugWorkaround() {
    super();
  }

  /**
   * Runs before each test; sets the {@link #mojo} field to a new
   * {@link ListEntityClassnamesMojo} instance.
   *
   * @see #mojo
   *
   * @see Before
   */
  @Before
  public void setUp() {
    this.mojo = new ListEntityClassnamesMojo();
  }

  /**
   * Tests the "sunny day" scenario for the bug workaround.
   */
  @Test
  public void testSunnyDayBugWorkaround() {
    final String text = "\"   foobar   \"";
    final String unquotedText = "   foobar   ";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  /**
   * Tests that single quoting a space-bounded {@link String} will
   * have its quotes stripped.
   */
  @Test
  public void testSingleQuotes() {
    final String text = "'   foobar   '";
    final String unquotedText = "   foobar   ";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  /**
   * Tests that mismatched quotes are not replaced.
   */
  @Test
  public void testBugWorkaroundMismatchedQuotes() {
    final String text = "\"   foobar   '";
    final String unquotedText = "\"   foobar   '";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  /**
   * Moves up an abstract layer and tests that the {@link
   * ListEntityClassnamesMojo}'s {@link
   * ListEntityClassnamesMojo#setSuffix(String) setSuffix(String)}
   * method applies the bug workaround.
   */
  @Test
  public void testSetSuffix() {
    final String suffix = "'  A test suffix      '";
    final String expectedSuffix = "  A test suffix      ";
    this.mojo.setSuffix(suffix);
    assertEquals(expectedSuffix, this.mojo.getSuffix());
  }

}