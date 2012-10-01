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

import org.apache.maven.model.Build; // for javadoc only

import org.apache.maven.project.MavenProject;

import org.scannotation.archiveiterator.DirectoryIteratorFactory;
import org.scannotation.archiveiterator.FileIterator;
import org.scannotation.archiveiterator.FileProtocolIteratorFactory;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.IteratorFactory;
import org.scannotation.archiveiterator.JarIterator;
import org.scannotation.archiveiterator.StreamIterator;

/**
 * An {@link AbstractMojo} that provides support for scanning a set of
 * {@link URL}s and reporting back on the annotated classnames found
 * there.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractJPAMojo extends AbstractMojo {

  /**
   * Static initializer; works around <a
   * href="http://sourceforge.net/tracker/?func=detail&aid=3134533&group_id=214374&atid=1029423">Scannotation
   * bug #3134533</a> by installing a patched {@link
   * FileProtocolIteratorFactory} into the {@link IteratorFactory}
   * class&apos; {@link IteratorFactory#registry} field.
   *
   * @see <a
   * href="http://sourceforge.net/tracker/?func=detail&aid=3134533&group_id=214374&atid=1029423">Scannotation
   * bug #3134533</a>
   */
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
   * The {@link MavenProject} usually injected by the Maven runtime.
   * Used for the return value of its {@link
   * MavenProject#getTestClasspathElements()
   * getTestClasspathElements()} method and its associated {@link
   * Build}'s {@link Build#getTestOutputDirectory()
   * getTestOutputDirectory()} method.  This field may be {@code null}
   * when this {@link AbstractJPAMojo} is not <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">configured
   * by Maven</a>.
   *
   * @parameter default-value="${project}" property="project"
   *
   * @readonly
   *
   * @required
   *
   * @see <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">Guide
   * to Configuring Plug-ins</a>
   */
  private MavenProject project;

  /**
   * The {@link AnnotationDB} that will be {@linkplain
   * #cloneAnnotationDB() cloned} for use by this {@link
   * AbstractJPAMojo}.  This field may be {@code null} at any point,
   * and may be populated by either <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">Maven</a>,
   * the {@link #setAnnotationDB(AnnotationDB)} method or the {@link
   * #createAnnotationDB()} method.
   *
   * @parameter alias="db" property="annotationDB"
   *
   * @see #cloneAnnotationDB()
   *
   * @see #createAnnotationDB()
   *
   * @see #setAnnotationDB(AnnotationDB)
   *
   * @see <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">Guide
   * to Configuring Plug-ins</a>
   */
  private AnnotationDB db;

  /**
   * A {@link URLFilter} that will be used to construct the {@link
   * Set} of {@link URL}s that will be scanned by this {@link
   * AbstractJPAMojo}.  This field may be {@code null} at any point
   * and may be populated by either <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">Maven</a>
   * or the {@link #setURLFilter(URLFilter)} method.
   *
   * @parameter property="URLFilter"
   *
   * @see #getURLFilter()
   *
   * @see #setURLFilter(URLFilter)
   *
   * @see <a
   * href="http://maven.apache.org/guides/mini/guide-configuring-plugins.html">Guide
   * to Configuring Plug-ins</a>
   */
  private URLFilter urlFilter;

  /**
   * Constructs a new {@link AbstractJPAMojo}.  No configuration
   * automatic or otherwise will have taken place as a result of
   * calling this constructor.
   */
  protected AbstractJPAMojo() {
    super();
  }

  /**
   * Creates a new {@link AnnotationDB} in the (common) case where a
   * user has not supplied this {@link AbstractJPAMojo} with a
   * pre-configured {@link AnnotationDB}.
   *
   * <p>This method never returns {@code null}.  Subclasses overriding
   * this method must ensure that their overridden implementation
   * never returns {@code null}.</p>
   *
   * @return a new {@link AnnotationDB}; never {@code null}
   *
   * @see AnnotationDB
   *
   * @see org.scannotation.AnnotationDB
   */
  protected AnnotationDB createAnnotationDB() {
    return new AnnotationDB();
  }

  /**
   * Returns this {@link AbstractJPAMojo}'s associated {@link
   * URLFilter}, or {@code null} if no such {@link URLFilter} exists.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link URLFilter} used by this {@link
   * AbstractJPAMojo}, or {@code null}
   *
   * @see #setURLFilter(URLFilter)
   *
   * @see URLFilter
   */
  public URLFilter getURLFilter() {
    return this.urlFilter;
  }

  /**
   * Sets this {@link AbstractJPAMojo}'s associated {@link URLFilter}.
   * {@code null} is permitted as a parameter value.
   *
   * @param filter the {@link URLFilter} to set; may be {@code null}
   *
   * @see #getURLFilter()
   *
   * @see URLFilter
   */
  public void setURLFilter(final URLFilter filter) {
    this.urlFilter = filter;
  }

  /**
   * Returns the {@link MavenProject} that Maven customarily injects
   * into this mojo, or {@code null} if no such {@link MavenProject}
   * has been set.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link MavenProject} associated with this mojo, or
   * {@code null}
   */
  public MavenProject getProject() {
    return this.project;
  }

  /**
   * Installs the {@link MavenProject} for use by this mojo during its
   * run.
   *
   * @param project the {@link MavenProject} to use; may be {@code
   * null}
   */
  public void setProject(final MavenProject project) {
    this.project = project;
  }

  /**
   * Returns a {@linkplain AnnotationDB#clone() clone} of this {@link
   * AbstractJPAMojo}'s {@linkplain #setAnnotationDB(AnnotationDB)
   * associated <tt>AnnotationDB</tt>}.
   *
   * <p>A clone is returned because {@link
   * org.scannotation.AnnotationDB} retains state after {@linkplain
   * org.scannotation.AnnotationDB#scanArchives(URL[]) scanning}, and
   * Maven plugins have no contractually defined lifecycle semantics.
   * Consequently it is unknown how long-lived this {@link
   * AbstractJPAMojo}'s {@link #db} reference might be.</p>
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return a {@linkplain AnnotationDB#clone() clone} of this {@link
   * AbstractJPAMojo}'s {@linkplain #setAnnotationDB(AnnotationDB)
   * associated <tt>AnnotationDB</tt>}, or {@code null} if no such
   * {@link AnnotationDB} could be cloned
   *
   * @see #setAnnotationDB(AnnotationDB)
   * 
   * @see AnnotationDB
   *
   * @see AnnotationDB#clone()
   *
   * @see org.scannotation.AnnotationDB
   *
   * @see org.scannotation.AnnotationDB#annotationIndex
   *
   * @see org.scannotation.AnnotationDB#classIndex
   */
  public final AnnotationDB cloneAnnotationDB() {
    if (this.db == null) {
      this.db = this.createAnnotationDB();
    }
    if (this.db == null) {
      return null;
    }
    return this.db.clone();
  }

  /**
   * Sets the {@link AnnotationDB} that will be used by this {@link
   * AbstractJPAMojo}'s {@link #cloneAnnotationDB()} method.  {@code
   * null} is permitted as a parameter value.
   *
   * @param db the {@link AnnotationDB} to set; may be {@code null}
   *
   * @see #cloneAnnotationDB()
   *
   * @see AnnotationDB
   */
  public void setAnnotationDB(final AnnotationDB db) {
    this.db = db;
  }

  /**
   * Scans the supplied {@link Set} of {@link URL}s and returns the
   * {@link AnnotationDB} that contains the scanned annotation
   * information.
   *
   * <p>This method may return {@code null} in exceptional
   * circumstances.</p>
   *
   * @param urls the {@link Set} of {@link URL}s to scan; if {@code
   * null}, then no scanning operation will take place
   *
   * @return the {@link AnnotationDB} that was used to perform the
   * scan, or {@code null} if no {@link AnnotationDB} could be
   * {@linkplain #cloneAnnotationDB() found}
   *
   * @exception IOException if an error occurs during scanning
   *
   * @see #cloneAnnotationDB()
   *
   * @see org.scannotation.AnnotationDB#scanArchives(URL[])
   */
  protected final AnnotationDB scan(final Set<URL> urls) throws IOException {
    final AnnotationDB db = this.cloneAnnotationDB();
    final AnnotationDB result = this.scan(db, urls);
    assert result == db;
    return result;
  }

  /**
   * Scans the supplied {@link Set} of {@link URL}s and as a
   * convenience returns the supplied {@link AnnotationDB} that
   * contains the scanned annotation information.
   *
   * <p>This method may return {@code null} if the supplied {@code db}
   * is {@code null}.</p>
   *
   * @param db the {@link AnnotationDB} used to {@linkplain
   * org.scannotation.AnnotationDB#scanArchives(URL[]) perform the
   * scan}; if {@code null} then no scanning operation will take place
   *
   * @param urls the {@link Set} of {@link URL}s to scan; if {@code
   * null}, then no scanning operation will take place
   *
   * @return the {@code db} parameter
   *
   * @exception IOException if an error occurs during scanning
   *
   * @see org.scannotation.AnnotationDB#scanArchives(URL[])
   */
  private final AnnotationDB scan(AnnotationDB db, final Set<URL> urls) throws IOException {
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