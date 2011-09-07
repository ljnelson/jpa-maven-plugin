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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.DependencyResolutionRequiredException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.project.MavenProject;

import org.apache.maven.model.Build;

/**
 * @requiresDependencyResolution test
 * @goal list-entity-classnames
 */
public abstract class ListEntityClassnamesMojo extends AbstractJPAMojo {

  private static final List<String> JPA_ANNOTATIONS = Arrays.asList("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embeddable", "javax.persistence.IdClass");

  /**
   * @parameter default-value="UTF8"
   * @required
   */
  private String encoding;

  /**
   * @parameter default-value="${project.build.directory}${file.separator}generated-test-sources${file.separator}jpa-maven-plugin${file.separator}entityClassnames.properties"
   * @required
   */
  protected File outputFile;

  /**
   * @parameter default-value="entityClassnames"
   * @required
   */
  private String defaultPropertyName;

  /**
   * @parameter
   */
  private Map<String, String> propertyNames;

  /**
   * @parameter
   */
  private String firstItemPrefix;
  
  /**
   * @parameter default-value="<class>"
   */
  private String prefix;
  
  /**
   * @parameter default-value="</class>${line.separator}"
   */
  private String suffix;
  
  /**
   * @parameter
   */
  private String lastItemSuffix;

  /**
   * @parameter
   */
  private Set<URL> urls;

  private final Set<URL> initializeURLs() throws MojoFailureException, MojoExecutionException {
    if (this.urls == null) {
      final Log log = this.getLog();
      assert log != null;
      final List<?> classpathElements;
      List<?> temp = null;
      try {
        temp = this.project.getTestClasspathElements();
      } catch (final DependencyResolutionRequiredException dependencyResolutionRequiredException) {
        throw new MojoFailureException(String.format("While trying to obtain the test classpath elements, a DependencyResolutionRequiredException was encountered."), dependencyResolutionRequiredException);
      } finally {
        classpathElements = temp;
      }
      Set<URL> urls = new LinkedHashSet<URL>(classpathElements == null ? 0 : classpathElements.size());
      if (classpathElements != null && !classpathElements.isEmpty()) {
        for (final Object o : classpathElements) {
          if (o != null) {
            final File file = new File(o.toString());
            if (file.canRead()) {
              final URL url;
              URL tempURL = null;
              try {
                tempURL = file.toURI().toURL();
              } catch (final MalformedURLException wontHappen) {
                throw new MojoExecutionException(String.format("While attempting to convert a file, %s, into a URL, a MalformedURLException was encountered.", file), wontHappen);
              } finally {
                url = tempURL;
              }
              if (url != null) {
                urls.add(url);
              }
            } else if (log.isWarnEnabled()) {
              log.warn(String.format("The test classpath element %s could not be read.", file));
            }
          }
        }
      } else if (log.isWarnEnabled()) {
        log.warn(String.format("The test classpath contained no elements. Consequently no Entities were found."));
      }
      if (log.isWarnEnabled() && urls.isEmpty()) {
        log.warn(String.format("No URLs were found from the test classpath (%s).", classpathElements));
      }
      this.urls = urls;
    }
    assert this.urls != null;
    final URLFilter urlFilter = this.getURLFilter();
    final Iterator<URL> iterator = this.urls.iterator();
    assert iterator != null;
    while (iterator.hasNext()) {
      final URL url = iterator.next();
      if (url == null || (urlFilter != null && !urlFilter.accept(url))) {
        iterator.remove();
      }
    }
    return this.urls;
  }

  private final String getProjectBuildOutputDirectoryName() throws MojoFailureException, MojoExecutionException {
    if (this.project == null) {
      throw new MojoExecutionException("this.project == null");
    }
    final Build build = this.project.getBuild();
    if (build == null) {
      throw new MojoExecutionException("this.project.getBuild() == null");
    }
    final String outputDirectoryName = build.getOutputDirectory();
    if (outputDirectoryName == null) {
      throw new MojoExecutionException("this.project.getBuild().getOutputDirectory() == null");
    }
    return outputDirectoryName;
  }

  private final File initializeOutputFile() throws MojoFailureException, MojoExecutionException {
    if (this.outputFile == null) {
      final File projectBuildOutputDirectory = new File(this.getProjectBuildOutputDirectoryName());
      final File outputDirectory = new File(projectBuildOutputDirectory, "generated-test-sources/jpa-maven-plugin");
      this.validateOutputDirectory(outputDirectory);
      this.outputFile = new File(outputDirectory, "entityClassnames.properties");
    } else {
      if (!this.outputFile.isAbsolute()) {
        final File projectBuildOutputDirectory = new File(this.getProjectBuildOutputDirectoryName());
        final File outputDirectory = new File(projectBuildOutputDirectory, "generated-test-sources/jpa-maven-plugin");
        this.validateOutputDirectory(outputDirectory);
        this.outputFile = new File(outputDirectory, this.outputFile.getPath());
      }
      if (this.outputFile.isDirectory()) {
        final File outputDirectory = this.outputFile;
        this.validateOutputDirectory(outputDirectory);
        this.outputFile = new File(outputDirectory, "entityClassnames.properties");
      } else if (this.outputFile.exists()) {
        if (!this.outputFile.isFile()) {
          throw new MojoExecutionException(String.format("The outputFile specified, %s, is not a directory, but is also not a regular file.  The outputFile parameter must deisgnate either an existing, writable file or a non-existent file.", this.outputFile));
        } else if (!this.outputFile.canWrite()) {
          throw new MojoExecutionException(String.format("The outputFile specified, %s, is a regular file, but cannot be written to by Maven running as user %s.  The outputFile parameter must designate either an existing, writable file or a non-existent file.", this.outputFile, System.getProperty("user.name")));
        } else {
          this.validateOutputDirectory(this.outputFile.getParentFile());
        }
      } else {
        this.validateOutputDirectory(this.outputFile.getParentFile());
      }
    }
    assert this.outputFile != null;
    assert this.outputFile.isAbsolute();
    final Log log = this.getLog();
    if (log != null && log.isDebugEnabled()) {
      log.debug(String.format("Output file initialized to %s", this.outputFile));
    }
    return this.outputFile;
  }
  
  private void validateOutputDirectory(final File outputDirectory) throws MojoFailureException, MojoExecutionException {
    if (outputDirectory == null) {
      throw new IllegalArgumentException("outputDirectory", new NullPointerException("outputDirectory == null"));
    } else if (outputDirectory.exists()) {
      if (!outputDirectory.isDirectory()) {
        throw new MojoFailureException(String.format("The output directory path, %s, exists but is not a directory.", outputDirectory));
      }
      if (!outputDirectory.canWrite()) {
        throw new MojoFailureException(String.format("The output directory path, %s, exists and is a directory, but Maven running as %s cannot write to it.", outputDirectory, System.getProperty("user.name")));
      }
    } else {
      final boolean directoryCreationSuccess = outputDirectory.mkdirs();
      if (!directoryCreationSuccess) {
        throw new MojoFailureException(String.format("The output directory path, %s, does not exist because the attempt to create it failed.", outputDirectory));
      }
    }
  }

  private void scrubParameters() {
    if (this.encoding == null) {
      this.encoding = "";
    } else {
      this.encoding = this.encoding.trim();
    }
    if (this.encoding.isEmpty()) {
      this.encoding = "UTF8";
    }

    if (this.propertyNames == null) {
      this.propertyNames = new HashMap<String, String>();
    }

    if (this.defaultPropertyName == null) {
      this.defaultPropertyName = "";
    } else {
      this.defaultPropertyName = this.defaultPropertyName.trim();
    }
    if (this.defaultPropertyName.isEmpty()) {
      this.defaultPropertyName = "entityClassnames";
    }

    if (this.firstItemPrefix == null) {
      this.firstItemPrefix = "";
    }

    if (this.prefix == null) {
      this.prefix = "";
    }

    if (this.suffix == null) {
      this.suffix = "";
    }

    if (this.lastItemSuffix == null) {
      this.lastItemSuffix = "";
    }

  }

  public File getOutputFile() {
    return this.outputFile;
  }

  public void setOutputFile(final File file) {
    this.outputFile = file;
  }

  public Set<URL> getURLs() {
    return this.urls;
  }

  public void setURLs(final Set<URL> urls) {
    this.urls = urls;
  }

  private final AnnotationDB scan() throws MojoExecutionException, MojoFailureException {
    AnnotationDB db = this.cloneAnnotationDB();
    final Set<URL> urls = this.getURLs();
    if (urls == null || urls.isEmpty()) {
      final Log log = this.getLog();
      if (log != null && log.isWarnEnabled()) {
        log.warn(String.format("There are no URLs to scan."));
      }
    } else {
      if (db == null) {
        throw new MojoExecutionException("this.cloneAnnotationDB() == null");
      }
      try {
        db = this.scan(db, urls);
      } catch (final IOException ioException) {
        throw new MojoExecutionException(String.format("While trying to scan the test classpath (%s) for Entities, an IOException was encountered.", urls), ioException);
      }
    }
    return db;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = this.getLog();
    if (log == null) {
      throw new MojoExecutionException("this.getLog() == null");
    }
    this.scrubParameters();
    this.initializeURLs();

    final File outputFile = this.initializeOutputFile();
    assert outputFile != null;

    // Scan the test classpath for Entity, MappedSuperclass, IdClass,
    // etc. annotations.
    final AnnotationDB db = this.scan();
    assert db != null;
    if (log.isDebugEnabled()) {
      log.debug("Annotation index:");
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      db.outputAnnotationIndex(pw);
      log.debug(sw.toString());
      try {
        sw.close();
      } catch (final IOException ignored) {
        // ignored on purpose
      }
      pw.close();
    }

    final Properties properties = new Properties();

    // Having scanned the classpaths, get the "index", which is a Map
    // of classnames indexed by annotation classnames.
    final Map<String, Set<String>> ai = db.getAnnotationIndex();
    if (ai == null) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("After scanning for Entities, a null annotation index was returned by the AnnotationDB."));
      }
    } else if (ai.isEmpty()) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("After scanning for Entities, no annotated Entities were found."));
      }
    } else {

      assert this.propertyNames != null;
      assert this.defaultPropertyName != null;
      this.propertyNames.put("", this.defaultPropertyName);
      
      final Map<String, Set<String>> propertyNameIndex = new HashMap<String, Set<String>>();
      
      // For each of the annotations we are interested in, do some
      // work on the classes that sport those annotations.
      for (final String jpaAnnotation : JPA_ANNOTATIONS) {

        // Find all classnames annotated with that annotation
        // (e.g. @Entity, @MappedSuperclass, etc.).
        final Set<String> annotatedClassNames = ai.get(jpaAnnotation);

        if (annotatedClassNames != null && !annotatedClassNames.isEmpty()) {

          for (final String annotatedClassName : annotatedClassNames) {
            assert annotatedClassName != null;
            
            // For every classname we find, see which property name it
            // is going to be assigned to.  For example, we might be
            // configured so that com.foobar.* get assigned to the
            // foobarClassnames property.
            final String propertyName = this.determinePropertyName(annotatedClassName);
            assert propertyName != null;
            
            Set<String> relevantClassNames = propertyNameIndex.get(propertyName);
            if (relevantClassNames == null) {
              relevantClassNames = new TreeSet<String>();
              propertyNameIndex.put(propertyName, relevantClassNames);
            }
            assert relevantClassNames != null;
            
            // Add the annotated class to the set of other annotated
            // classnames stored under that property.
            relevantClassNames.add(annotatedClassName);
            
          }
        }
      }

      final Set<Entry<String, Set<String>>> entrySet = propertyNameIndex.entrySet();
      assert entrySet != null;
      for (final Entry<String, Set<String>> entry : entrySet) {
        assert entry != null;

        // For every entry indexing a set of classes under a property
        // name, stringify the set of classnames into a single
        // StringBuilder.  Index that stringified set under the
        // property name.  This Properties will be the contents of our file.

        final StringBuilder sb = new StringBuilder();

        final String propertyName = entry.getKey();
        assert propertyName != null;

        final Set<String> classNames = entry.getValue();
        assert classNames != null;

        final Iterator<String> classNamesIterator = classNames.iterator();
        assert classNamesIterator != null;
        assert classNamesIterator.hasNext();

        while (classNamesIterator.hasNext()) {
          final String className = classNamesIterator.next();
          assert className != null;
          sb.append(this.decorate(className, sb.length() <= 0, classNamesIterator.hasNext()));
        }

        properties.setProperty(propertyName, sb.toString());

      }

    }

    if (log.isDebugEnabled()) {
      final Enumeration<?> propertyNames = properties.propertyNames();
      if (propertyNames != null) {
        while (propertyNames.hasMoreElements()) {
          final String key = propertyNames.nextElement().toString();
          final String value = properties.getProperty(key);
          log.debug(key + " = " + value);
        }
      }
    }

    Writer writer = null;
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile), this.encoding));
      properties.store(writer, "Generated by " + this.getClass().getName());
      writer.flush();
    } catch (final IOException kaboom) {
      throw new MojoExecutionException(String.format("While attempting to write to the outputFile parameter (%s), an IOException was encountered."), kaboom);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (final IOException ignore) {
          
        }
      }
    }

  }

  private final String determinePropertyName(final String className) {
    String propertyName = this.defaultPropertyName;
    if (className != null && !className.isEmpty()) {
      final Log log = this.getLog();
      assert log != null;

      final int index = Math.max(0, className.lastIndexOf('.'));
      String packageName = className.substring(0, index);
      assert packageName != null;
      
      log.debug("Package: " + packageName);
      
      propertyName = this.propertyNames.get(packageName);
      while (propertyName == null && packageName != null && !packageName.isEmpty()) {
        final int dotIndex = Math.max(0, packageName.lastIndexOf('.'));
        packageName = packageName.substring(0, dotIndex);
        log.debug("Package: " + packageName);
        propertyName = this.propertyNames.get(packageName);
      }
      
      log.debug("propertyName: " + propertyName);
    }
    return propertyName;
  }

  protected String decorate(final String classname, final boolean first, final boolean more) {
    if (classname == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    if (!first) {
      sb.append(this.prefix);
    } else {
      sb.append(this.firstItemPrefix);
    }
    sb.append(classname);
    if (more) {
      sb.append(this.suffix);
    } else {
      sb.append(this.lastItemSuffix);
    }
    return sb.toString();
  }

  @Override
  protected AnnotationDB createAnnotationDB() {
    final AnnotationDB db = new AnnotationDB();
    db.setScanClassAnnotations(true);
    db.setScanMethodAnnotations(false);
    db.setScanParameterAnnotations(false);
    db.setScanFieldAnnotations(false);
    return db;
  }

}