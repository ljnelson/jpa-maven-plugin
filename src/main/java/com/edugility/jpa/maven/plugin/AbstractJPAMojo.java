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

import java.lang.reflect.Field;

import java.net.URISyntaxException;
import java.net.URL;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.project.MavenProject;

import org.scannotation.archiveiterator.DirectoryIteratorFactory;
import org.scannotation.archiveiterator.FileIterator;
import org.scannotation.archiveiterator.FileProtocolIteratorFactory;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.IteratorFactory;
import org.scannotation.archiveiterator.JarIterator;
import org.scannotation.archiveiterator.StreamIterator;

public abstract class AbstractJPAMojo extends AbstractMojo {

  static {
    Field field = null;
    try {
      field = IteratorFactory.class.getDeclaredField("registry");
      assert field != null;
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      final Map<String, DirectoryIteratorFactory> registry = (Map<String, DirectoryIteratorFactory>)field.get(null);
      assert registry != null;
      assert registry.containsKey("file");
      final Object old = registry.put("file", new FileProtocolIteratorFactory() {
          @Override
          public StreamIterator create(final URL url, final Filter filter) throws IOException {
            StreamIterator returnValue = null;
            if (url != null) {
              // See http://sourceforge.net/tracker/?func=detail&aid=3134533&group_id=214374&atid=1029423
              File file;
              try {
                file = new File(url.toURI());
              } catch (final URISyntaxException e) {
                file = new File(url.getPath());
              }
              if (file.isDirectory()) {
                returnValue = new FileIterator(file, filter);
              } else {
                returnValue = new JarIterator(url.openStream(), filter);
              }
            }
            return returnValue;
          }
        });
      assert old != null;
    } catch (final Exception ohWell) {
      ohWell.printStackTrace();
    }
  }

  /**
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * @parameter
   */
  protected AnnotationDB db;

  /**
   * @parameter
   */
  protected URLFilter urlFilter;

  protected AbstractJPAMojo() {
    super();
  }

  protected AnnotationDB createAnnotationDB() {
    return new AnnotationDB();
  }

  public URLFilter getURLFilter() {
    return this.urlFilter;
  }

  public void setURLFilter(final URLFilter filter) {
    this.urlFilter = filter;
  }

  public AnnotationDB cloneAnnotationDB() {
    if (this.db == null) {
      return this.createAnnotationDB();
    } else {
      return this.db.clone();
    }
  }

  public void setAnnotationDB(final AnnotationDB db) {
    this.db = db;
  }

  protected final AnnotationDB scan(final Set<URL> urls) throws DependencyResolutionRequiredException, IOException {
    return this.scan(this.cloneAnnotationDB(), urls);
  }

  protected AnnotationDB scan(AnnotationDB db, final Set<URL> urls) throws IOException {
    if (db != null && urls != null && !urls.isEmpty()) {
      final Log log = this.getLog();
      if (log != null && log.isDebugEnabled()) {
        log.debug("Scanning the following URLs: " + urls);
      }
      db.clear();
      db.scanArchives(urls.toArray(new URL[urls.size()]));
    }
    return db;
  }

}