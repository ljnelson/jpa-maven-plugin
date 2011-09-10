/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * $Id$
 *
 * Copyright (c) 2011 Edugility LLC.
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

import java.lang.annotation.ElementType; // for javadoc only

import java.util.HashMap;
import java.util.Set;

/**
 * An {@link org.scannotation.AnnotationDB} subclass that adds {@link
 * Cloneable} support and the ability to {@linkplain #clear() clear
 * state}.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 */
public class AnnotationDB extends org.scannotation.AnnotationDB implements Cloneable {
  
  /**
   * Creates a new {@link AnnotationDB}.
   */
  public AnnotationDB() {
    super();
  }
  
  /**
   * Creates a new {@link AnnotationDB}, performing a deep copy of the
   * state of the supplied {@link AnnotationDB}.
   *
   * @param db the {@link AnnotationDB} whose state should be copied;
   * may be {@code null}
   */
  public AnnotationDB(final AnnotationDB db) {
    super();
    this.copyState(db);
  }
  
  /**
   * Deeply copies all known state from the supplied {@link
   * AnnotationDB} to this one.
   *
   * <p>Specifically, this method copies the {@link
   * #getAnnotationIndex() annotationIndex}, {@link #getClassIndex()
   * classIndex} and {@link
   * org.scannotation.AnnotationDB#implementsIndex} properties into
   * new data structures.  The elements within these data structures
   * are not cloned.</p>
   *
   * <p>This method also copies the {@code boolean} properties ({@link
   * #getScanClassAnnotations scanClassAnnotations} and the like).</p>
   *
   * @param db the {@link AnnotationDB} whose state should be copied;
   * may be {@code null} in which case no operation takes place
   */
  private final void copyState(final AnnotationDB db) {
    if (db != null) {
      this.annotationIndex = new HashMap<String, Set<String>>(db.getAnnotationIndex());
      this.implementsIndex = new HashMap<String, Set<String>>(db.implementsIndex);
      this.classIndex = new HashMap<String, Set<String>>(db.getClassIndex());
      
      this.setScanClassAnnotations(db.getScanClassAnnotations());
      this.setScanMethodAnnotations(db.getScanMethodAnnotations());
      this.setScanParameterAnnotations(db.getScanParameterAnnotations());
      this.setScanFieldAnnotations(db.getScanFieldAnnotations());
      
      this.setIgnoredPackages((String[])db.getIgnoredPackages().clone());
    }
  }

  /**
   * Returns a deep copy of this {@link AnnotationDB}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a deep copy of this {@link AnnotationDB}; never {@code null}
   */
  @Override
  public AnnotationDB clone() {
    final AnnotationDB copy;
    AnnotationDB temp = null;
    try {
      temp = (AnnotationDB)super.clone();
    } catch (final CloneNotSupportedException error) {
      throw (InternalError)new InternalError().initCause(error);
    } finally {
      copy = temp;
    }
    copy.copyState(this);
    return copy;
  }

  /**
   * Clears all transient state from this {@link AnnotationDB}.
   */
  public void clear() {
    this.annotationIndex.clear();
    this.implementsIndex.clear();
    this.classIndex.clear();
  }

  /**
   * Returns whether this {@link AnnotationDB} should scan field
   * annotations.
   *
   * @return {@code true} if this {@link AnnotationDB} should scan
   * {@linkplain ElementType#FIELD field annotations}; {@code false}
   * otherwise
   */
  public boolean getScanFieldAnnotations() {
    return this.scanFieldAnnotations;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScanFieldAnnotations(final boolean scan) {
    super.setScanFieldAnnotations(scan);
  }

  /**
   * Returns whether this {@link AnnotationDB} should scan method
   * annotations.
   *
   * @return {@code true} if this {@link AnnotationDB} should scan
   * {@linkplain ElementType#METHOD method annotations}; {@code false}
   * otherwise
   */
  public boolean getScanMethodAnnotations() {
    return this.scanMethodAnnotations;
  }

  /**
   * Sets whether this {@link AnnotationDB} should scan {@linkplain
   * ElementType#METHOD method annotations}.
   *
   * @param scan whether to scan {@linkplain ElementType#METHOD method
   * annotations}
   */
  @Override
  public void setScanMethodAnnotations(final boolean scan) {
    super.setScanMethodAnnotations(scan);
  }

  /**
   * Returns whether this {@link AnnotationDB} should scan parameter
   * annotations.
   *
   * @return {@code true} if this {@link AnnotationDB} should scan
   * {@linkplain ElementType#PARAMETER parameter annotations}; {@code false}
   * otherwise
   */
  public boolean getScanParameterAnnotations() {
    return this.scanParameterAnnotations;
  }

  /**
   * Sets whether this {@link AnnotationDB} should scan {@linkplain
   * ElementType#PARAMETER parameter annotations}.
   *
   * @param scan whether to scan {@linkplain ElementType#PARAMETER
   * parameter annotations}
   */
  @Override
  public void setScanParameterAnnotations(final boolean scan) {
    super.setScanParameterAnnotations(scan);
  }

  /**
   * Returns whether this {@link AnnotationDB} should scan class annotations.
   *
   * @return {@code true} if this {@link AnnotationDB} should scan
   * {@linkplain ElementType#TYPE class annotations}; {@code false}
   * otherwise
   */
  public boolean getScanClassAnnotations() {
    return this.scanClassAnnotations;
  }

  /**
   * Sets whether this {@link AnnotationDB} should scan {@linkplain
   * ElementType#TYPE class annotations}.
   *
   * @param scan whether to scan {@linkplain ElementType#TYPE class
   * annotations}
   */
  @Override
  public void setScanClassAnnotations(final boolean scan) {
    super.setScanClassAnnotations(scan);
  }

}