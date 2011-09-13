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
package com.edugility.jpa.maven.plugin.test.project;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity; // for javadoc only
import javax.persistence.MappedSuperclass;

/**
 * A {@link MappedSuperclass @MappedSuperclass} for testing purposes.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public class SimpleMappedSuperclass {

  /**
   * The text of this {@link SimpleMappedSuperclass}.  This field
   * exists so that {@link Entity @Entities} that inherit from this
   * class will also inherit this column mapping.
   *
   * <p>This field may be {@code null} at any point.</p>
   */
  @Column(name = "text")
  private String text;

  /**
   * Returns the text of this {@link SimpleMappedSuperclass}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the text of this {@link SimpleMappedSuperclass}, or
   * {@code null}
   */
  public String getText() {
    return this.text;
  }

  /**
   * Sets the text.
   *
   * @param text the new text value; may be {@code null}
   */
  public void setText(final String text) {
    this.text = text;
  }

}