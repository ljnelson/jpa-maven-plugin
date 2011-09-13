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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A simple {@link Entity} for testing purposes.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 */
@Access(AccessType.FIELD)
@Entity
@Table(name = "simple_entity")
public class SimpleEntity extends SimpleMappedSuperclass {

  /**
   * The primary key ({@link Id @Id}) of this {@link SimpleEntity}.
   *
   * @see #SimpleEntity(long)
   */
  @Column(name = "id")
  @Id
  private long id;

  /**
   * Creates a new {@link SimpleEntity}.
   */
  protected SimpleEntity() {
    super();
  }

  /**
   * Creates a new {@link SimpleEntity}.
   *
   * @param id the value to use to uniquely identify the persistent
   * state of this {@link SimpleEntity}
   */
  public SimpleEntity(final long id) {
    this();
    this.id = id;
  }
  
  /**
   * Returns the {@link long} that identifies the persistent state of
   * this {@link SimpleEntity}.
   *
   * @return the persistent identifier of this {@link SimpleEntity}
   */
  public long getId() {
    return this.id;
  }

}