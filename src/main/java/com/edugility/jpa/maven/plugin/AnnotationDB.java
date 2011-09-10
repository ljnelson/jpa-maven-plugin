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

import java.util.HashMap;
import java.util.Set;

public class AnnotationDB extends org.scannotation.AnnotationDB implements Cloneable {
  
  public AnnotationDB() {
    super();
  }

  public AnnotationDB(final AnnotationDB db) {
    super();
    this.copyState(db);
  }

  private void copyState(final AnnotationDB db) {
    this.annotationIndex = new HashMap<String, Set<String>>(db.getAnnotationIndex());
    this.implementsIndex = new HashMap<String, Set<String>>(db.implementsIndex);
    this.classIndex = new HashMap<String, Set<String>>(db.getClassIndex());

    this.setScanClassAnnotations(db.getScanClassAnnotations());
    this.setScanMethodAnnotations(db.getScanMethodAnnotations());
    this.setScanParameterAnnotations(db.getScanParameterAnnotations());
    this.setScanFieldAnnotations(db.getScanFieldAnnotations());

    this.setIgnoredPackages((String[])db.getIgnoredPackages().clone());
  }

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

  public void clear() {
    this.annotationIndex.clear();
    this.implementsIndex.clear();
    this.classIndex.clear();
  }

  public boolean getScanFieldAnnotations() {
    return this.scanFieldAnnotations;
  }

  @Override
  public void setScanFieldAnnotations(final boolean scan) {
    super.setScanFieldAnnotations(scan);
  }

  public boolean getScanMethodAnnotations() {
    return this.scanMethodAnnotations;
  }

  @Override
  public void setScanMethodAnnotations(final boolean scan) {
    super.setScanMethodAnnotations(scan);
  }

  public boolean getScanParameterAnnotations() {
    return this.scanParameterAnnotations;
  }

  @Override
  public void setScanParameterAnnotations(final boolean scan) {
    super.setScanParameterAnnotations(scan);
  }

  public boolean getScanClassAnnotations() {
    return this.scanClassAnnotations;
  }

  @Override
  public void setScanClassAnnotations(final boolean scan) {
    super.setScanClassAnnotations(scan);
  }

}