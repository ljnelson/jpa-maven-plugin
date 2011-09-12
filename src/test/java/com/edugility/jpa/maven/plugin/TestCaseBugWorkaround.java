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

public class TestCaseBugWorkaround {

  private ListEntityClassnamesMojo mojo;

  public TestCaseBugWorkaround() {
    super();
  }

  @Before
  public void setUp() {
    this.mojo = new ListEntityClassnamesMojo();
  }

  @Test
  public void testSunnyDayBugWorkaround() {
    final String text = "\"   foobar   \"";
    final String unquotedText = "   foobar   ";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  @Test
  public void testSingleQuotes() {
    final String text = "'   foobar   '";
    final String unquotedText = "   foobar   ";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  @Test
  public void testBugWorkaroundMismatchedQuotes() {
    final String text = "\"   foobar   '";
    final String unquotedText = "\"   foobar   '";
    final String result = this.mojo.stripQuotes(text);
    assertEquals(unquotedText, result);
  }

  @Test
  public void testSetSuffix() {
    final String suffix = "'  A test suffix      '";
    final String expectedSuffix = "  A test suffix      ";
    this.mojo.setSuffix(suffix);
    assertEquals(expectedSuffix, this.mojo.getSuffix());
  }

}