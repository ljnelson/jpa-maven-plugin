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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.net.URI; // for javadoc only
import java.net.URL;
import java.net.MalformedURLException;

import java.util.Arrays;
import java.util.Collection; // for javadoc only
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.persistence.Embeddable; // for javadoc only
import javax.persistence.Entity; // for javadoc only
import javax.persistence.IdClass; // for javadoc only
import javax.persistence.MappedSuperclass; // for javadoc only

import org.apache.maven.artifact.DependencyResolutionRequiredException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.project.MavenProject;

import org.apache.maven.model.Build;

/**
 * Generates a {@code .properties} file, suitable for use as a Maven
 * <a
 * href="http://maven.apache.org/plugins/maven-resources-plugin/examples/filter.html">filter</a>,
 * whose contents are the set of names of classes that have been
 * annotated with the {@link javax.persistence.Entity}, {@link
 * javax.persistence.MappedSuperclass}, {@link
 * javax.persistence.Embeddable} and {@link javax.persistence.IdClass}
 * annotations.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0-SNAPSHOT
 *
 * @requiresDependencyResolution test
 *
 * @goal list-entity-classnames
 *
 * @phase process-classes
 *
 * @see AbstractJPAMojo
 *
 * @see javax.persistence.Entity
 *
 * @see javax.persistence.MappedSuperclass
 *
 * @see javax.persistence.Embeddable
 *
 * @see javax.persistence.IdClass
 */
public class ListEntityClassnamesMojo extends AbstractJPAMojo {

  /**
   * A workaround for <a
   * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>;
   * a {@link Pattern} used to strip initial leading and (matching)
   * trailing quotes from a {@link String}.  Used indirectly by the
   * {@link #setPrefix(String)}, {@link #setSuffix(String)}, {@link
   * #setFirstItemPrefix(String)} and {@link
   * #setLastItemSuffix(String)} methods.
   *
   * @see #stripQuotes(String)
   */
  private static final Pattern quotePattern;

  /**
   * Static initializer; a workaround for <a
   * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>;
   * initializes the {@link #quotePattern} field while avoiding {@link
   * ExceptionInInitializerError}s.
   */
  static {
    Pattern temp = null;
    try {
      temp = Pattern.compile("(?s)^(['\"])(.+)\\1$");
    } catch (final PatternSyntaxException kaboom) {
      kaboom.printStackTrace();
    } finally {
      quotePattern = temp;
    }
  }

  /**
   * The property name to use for classnames that belong to the
   * default package when {@linkplain #getDefaultPropertyName()
   * another default property name} cannot be found.
   */
  private static final String DEFAULT_DEFAULT_PROPERTY_NAME = "entityClassnames";

  /**
   * The default {@linkplain File#getName() name} used in constructing
   * the {@link #outputFile} when no output file has been specified
   * and the return value of the {@link #getUseOutputFile()} method is
   * {@code true}.
   */
  private static final String DEFAULT_OUTPUT_FILENAME = String.format("%s.properties", DEFAULT_DEFAULT_PROPERTY_NAME);

  /**
   * The default subdirectory prefix that is <i>added to</i> the value
   * of the current {@linkplain MavenProject Maven project}'s
   * {@linkplain Build#getDirectory() build directory} when
   * constructing a prefix for non-absolute output file
   * specifications.
   *
   * <p>This field is package-private for unit testing purposes
   * only.</p>
   */
  static final String DEFAULT_SUBDIR_PREFIX = String.format("generated-sources%1$sjpa-maven-plugin", File.separator);

  /**
   * The {@link List} of <a
   * href="http://download.oracle.com/javaee/6/api/javax/persistence/package-summary.html">JPA</a>
   * annotations that this {@link ListEntityClassnamesMojo} scans for.
   * A class that has been annotated with one of these annotations
   * must be made known to the JPA persistence unit in some fashion,
   * which is the task with which this mojo provides assistance.
   *
   * @see Embeddable
   *
   * @see Entity
   *
   * @see IdClass
   *
   * @see MappedSuperclass
   *
   * @see <a href="http://jcp.org/en/jsr/detail?id=317">Java
   * Persistence 2.0 Specification</a>
   */
  private static final List<String> JPA_ANNOTATIONS = Arrays.asList(Entity.class.getName(), MappedSuperclass.class.getName(), Embeddable.class.getName(), IdClass.class.getName());

  /**
   * A workaround for <a
   * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>;
   * if {@code true} then values for the {@link #getPrefix() prefix},
   * {@link #getSuffix() suffix}, {@link #getFirstItemPrefix()
   * firstItemPrefix} and {@link #getLastItemSuffix() lastItemSuffix}
   * will have any leading and trailing quotes (if they are a matched
   * pair) removed.  This should protect these values from undesired
   * trimming by Maven.
   *
   * @parameter default-value="true"
   */
  private boolean stripQuotes;

  /**
   * The character encoding to use when writing the {@link
   * #outputFile}.  The default value as configured by Maven will be
   * {@code ${project.build.sourceEncoding}}.  This field may be
   * {@code null} at any point.
   *
   * @parameter default-value="${project.build.sourceEncoding}" property="encoding"
   */
  private String encoding;

  /**
   * The {@link File} to which entity- and mapped superclass-annotated
   * classnames will be written.  This field may be {@code null} at
   * any point.  If this {@link File} is found to be relative, it will
   * be relative to
   * <tt>${project.build.directory}${file.separator}generated-sources${file.separator}jpa-maven-plugin${file.separator}</tt>.
   *
   * @parameter
   * default-value="${project.build.directory}${file.separator}generated-sources${file.separator}jpa-maven-plugin${file.separator}entityClassnames.properties"
   * property="outputFile"
   */
  private File outputFile;

  /**
   * Whether or not to write properties to an external file.
   *
   * @parameter default-value="true" property="useOutputFile"
   */
  boolean useOutputFile;

  /**
   * The property key under which the entity classname listing will be
   * stored.  Maven will configure this by default to be {@code
   * entityClassnames}.  This field may be {@code null} at any point.
   *
   * @parameter default-value="entityClassnames"
   * property="defaultPropertyName"
   */
  private String defaultPropertyName;

  /**
   * A {@link Map} of property names indexed by package prefix
   * segments.  Class names found belonging to packages that start
   * with the given package prefix segment will be stored in the
   * {@link #outputFile} indexed by the corresponding property name.
   *
   * <p>Segments in package names are delimited with a period ({@code
   * .}).  The following are examples of package prefix segments:</p>
   *
   * <ul>
   *
   * <li>{@code com.foobar.biz}</li>
   *
   * <li>{@code com.foobar}</li>
   *
   * <li>{@code com}</li>
   *
   * </ul>
   *
   * @parameter property="propertyNames"
   */
  private Map<String, String> propertyNames;

  /**
   * The textual prefix to prepend to the list of classnames.
   * 
   * @parameter default-value="" property="firstItemPrefix"
   */
  private String firstItemPrefix;
  
  /**
   * The textual prefix to prepend to every element of the list of
   * classnames, excluding the first element.
   * 
   * @parameter default-value="<class>" property="prefix"
   */
  private String prefix;
  
  /**
   * The suffix to append to every element of the list of classnames,
   * excluding the last element.  be {@code null} at any point.
   *
   * @parameter default-value="</class>${line.separator}"
   * property="suffix"
   */
  private String suffix;
  
  /**
   * The suffix to append to the list of classnames.
   *
   * @parameter default-value="" property="lastItemSuffix"
   */
  private String lastItemSuffix;

  /**
   * The {@link Set} of {@link URL}s to scan.  If not explicitly
   * specified, this mojo will scan the compile classpath.
   * 
   * @parameter property="URLs"
   */
  private Set<URL> urls;

  /**
   * Creates a new {@link ListEntityClassnamesMojo}.
   */
  public ListEntityClassnamesMojo() {
    super();
    this.stripQuotes = true;
    this.setDefaultPropertyName(DEFAULT_DEFAULT_PROPERTY_NAME);
    this.setFirstItemPrefix("");
    this.setPrefix("");
    this.setSuffix("");
    this.setLastItemSuffix("");
  }

  /**
   * Returns a {@link Map} of property names indexed by package
   * fragments.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return a {@link Map} of property names indexed by package
   * fragments, or {@code null}
   */
  public Map<String, String> getPropertyNames() {
    return this.propertyNames;
  }

  /**
   * Sets the {@link Map} of property names indexed by package
   * fragments that will be used to {@linkplain
   * #determinePropertyName(String) determine} under which property
   * name a given class name should be listed.
   *
   * <p><strong>Note:</strong> it is technically permissible for the
   * {@link #determinePropertyName(String)} method to be overridden
   * such that this {@link Map} is ignored.</p>
   *
   * @param propertyNames the {@link Map} of property names indexed by
   * package fragments; may be {@code null} in which case the
   * {@linkplain #getDefaultPropertyName() default property name} will
   * be used for all classes
   */
  public void setPropertyNames(final Map<String, String> propertyNames) {
    this.propertyNames = propertyNames;
  }

  /**
   * A workaround for <a
   * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>;
   * strips leading and trailing quotes from the supplied {@code text}
   * parameter value and returns the result.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>This method only does something if the {@link #stripQuotes}
   * field is set to {@code true}.</p>
   *
   * <p>This method is package-private for testing only.</p>
   *
   * @param text the text to strip; may be {@code null} in which case
   * no substitution will occur
   *
   * @return the supplied {@code text} with leading and trailing
   * quotes stripped, or {@code null} if the supplied {@code text} was
   * {@code null}
   *
   * @see <a
   * href="http://jira.codehaus.org/browse/MODELLO-256">MODELLO-256</a>
   */
  final String stripQuotes(String text) {
    if (text != null && this.stripQuotes && quotePattern != null) {
      final Matcher matcher = quotePattern.matcher(text);
      assert matcher != null;
      final StringBuffer sb = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(sb, matcher.group(2));
      }
      matcher.appendTail(sb);
      text = sb.toString();
    }
    return text;
  }

  /**
   * Returns the prefix prepended to every element of the list of
   * classnames, excluding the first element.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the prefix prepended to every element of the list of
   * classnames, excluding the first element, or {@code null}
   *
   * @see #setPrefix(String)
   */
  public String getPrefix() {
    return this.prefix;
  }

  /**
   * Sets the prefix prepended to every element of the list of
   * classnames, excluding the first element.
   *
   * @param prefix the prefix in question; may be {@code null}
   *
   * @see #getPrefix()
   *
   * @see #setFirstItemPrefix(String)
   */
  public void setPrefix(final String prefix) {
    // See http://jira.codehaus.org/browse/MODELLO-256.
    this.prefix = this.stripQuotes(prefix);
  }

  /**
   * Returns the prefix prepended to the list of classnames.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the prefix prepended to the list of classnames, or {@code
   * null}
   *
   * @see #getPrefix()
   *
   * @see #setFirstItemPrefix(String)
   */
  public String getFirstItemPrefix() {
    return this.firstItemPrefix;
  }

  /**
   * Sets the prefix prepended to the list of classnames.
   *
   * @param firstItemPrefix the prefix to be prepended to the list of
   * classnames; may be {@code null}
   *
   * @see #getFirstItemPrefix()
   *
   * @see #setPrefix(String)
   */
  public void setFirstItemPrefix(final String firstItemPrefix) {
    // See http://jira.codehaus.org/browse/MODELLO-256.
    this.firstItemPrefix = this.stripQuotes(firstItemPrefix);
  }

  /**
   * Returns the suffix appended to every element of the list of
   * classnames, excluding the last element.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the suffix appended to every element of the list of
   * classnames, excluding the last element, or {@code null}
   *
   * @see #setSuffix(String)
   */
  public String getSuffix() {
    return this.suffix;
  }
  
  /**
   * Sets the suffix appended to every element of the list of
   * classnames, excluding the last element.
   *
   * @param suffix the suffix in question; may be {@code null}
   *
   * @see #getSuffix()
   *
   * @see #setLastItemSuffix(String)
   */
  public void setSuffix(final String suffix) {
    // See http://jira.codehaus.org/browse/MODELLO-256.
    this.suffix = this.stripQuotes(suffix);
  }

  /**
   * Returns the suffix appended to the list of classnames.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the suffix appended to the list of classnames, or {@code
   * null}
   *
   * @see #getSuffix()
   *
   * @see #setLastItemSuffix(String)
   */
  public String getLastItemSuffix() {
    return this.lastItemSuffix;
  }

  /**
   * Sets the suffix appended to the list of classnames.
   *
   * @param lastItemSuffix the suffix to be appended to the list of
   * classnames; may be {@code null}
   *
   * @see #getLastItemSuffix()
   *
   * @see #setSuffix(String)
   */
  public void setLastItemSuffix(final String lastItemSuffix) {
    // See http://jira.codehaus.org/browse/MODELLO-256.
    this.lastItemSuffix = this.stripQuotes(lastItemSuffix);
  }

  /**
   * Initializes the {@link Set} of {@link URL}s to {@linkplain
   * #scan() scan} and returns it.
   *
   * <p>This method calls {@link #setURLs(Set)} as part of its
   * implementation.</p>
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link Set} of {@link URL}s that will be returned by
   * future calls to {@link #getURLs()}; never {@code null}
   */
  private final Set<URL> initializeURLs() throws DependencyResolutionRequiredException {
    Set<URL> urls = this.getURLs();
    if (urls == null || urls.isEmpty()) {
      urls = this.getClasspathURLs();
    }
    assert urls != null;
    final URLFilter urlFilter = this.getURLFilter();
    final Iterator<URL> iterator = urls.iterator();
    assert iterator != null;
    while (iterator.hasNext()) {
      final URL url = iterator.next();
      if (url == null || (urlFilter != null && !urlFilter.accept(url))) {
        iterator.remove();
      }
    }
    this.setURLs(urls);
    return this.getURLs();
  }

  /**
   * Returns a {@link Set} of {@link URL}s that represents the compile
   * classpath.
   *
   * <p>This uses the {@linkplain #getProject() associated
   * <tt>MavenProject</tt>} to {@linkplain
   * MavenProject#getCompileClasspathElements() supply the information}.
   * If that {@link MavenProject} is {@code null}, then an {@linkplain
   * Collection#isEmpty() empty} {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <tt>Set</tt>} is
   * returned.</p>
   *
   * <p>{@link String}-to-{@link URL} conversion is accomplished like
   * this:</p>
   *
   * <ul>
   *
   * <li>The {@link MavenProject#getCompileClasspathElements()} method
   * returns an untyped {@link List}.  There is no contractual
   * guarantee about the type of its contents.  Each element is
   * therefore treated as an {@link Object}.</li>
   *
   * <li>If the element is non-{@code null}, then its {@link
   * Object#toString()} method is invoked.  The resulting {@link
   * String} is used to {@linkplain File#File(String) construct a
   * <tt>File</tt>}.</li>
   *
   * <li>The resulting {@link File}'s {@link File#toURI()} method is
   * invoked and the {@linkplain URI result}'s {@link URI#toURL()}
   * method is invoked.  The return value is added to the {@link Set}
   * that will be returned.</li>
   *
   * </ul>
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a {@link Set} of {@link URL}s representing the compile
   * classpath, never {@code null}.  The {@link Set}'s iteration order
   * is guaranteed to be equal to that of the iteration order of the
   * return value of the {@link
   * MavenProject#getCompileClasspathElements()} method.
   *
   * @exception DependencyResolutionRequiredException if the {@link
   * MavenProject#getCompileClasspathElements()} method throws a {@link
   * DependencyResolutionRequiredException}
   */
  private final Set<URL> getClasspathURLs() throws DependencyResolutionRequiredException {
    final Set<URL> urls;

    final Log log = this.getLog();
    assert log != null;
    
    final MavenProject project = this.getProject();
    final List<?> classpathElements;
    if (project == null) {
      classpathElements = null;
    } else {
      classpathElements = project.getCompileClasspathElements();
    }

    if (classpathElements == null || classpathElements.isEmpty()) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("The compile classpath contained no elements. Consequently no Entities were found."));
      }
      urls = Collections.emptySet();
    } else {
      final Set<URL> mutableUrls = new LinkedHashSet<URL>(classpathElements.size());
      for (final Object o : classpathElements) {
        if (o != null) {
          final File file = new File(o.toString());
          if (file.canRead()) {
            try {
              mutableUrls.add(file.toURI().toURL());
            } catch (final MalformedURLException wontHappen) {
              throw (InternalError)new InternalError(String.format("While attempting to convert a file, %s, into a URL, a MalformedURLException was encountered.", file)).initCause(wontHappen);
            }
          } else if (log.isWarnEnabled()) {
            log.warn(String.format("The compile classpath element %s could not be read.", file));
          }
        }
      }
      if (mutableUrls.isEmpty()) {
        urls = Collections.emptySet();
      } else {
        urls = Collections.unmodifiableSet(mutableUrls);
      }
    }
    if (log.isWarnEnabled() && urls.isEmpty()) {
      log.warn(String.format("No URLs were found from the compile classpath (%s).", classpathElements));
    }
    return urls;
  }

  /**
   * Returns this {@link ListEntityClassnamesMojo}'s best guess as to
   * its {@linkplain #getProject() related Maven project}'s
   * {@linkplain Build#getDirectory() build directory}.  If this
   * {@link ListEntityClassnamesMojo} actually has a {@link
   * MavenProject} {@linkplain AbstractJPAMojo#getProject()
   * installed}, it will use the return value of that {@link
   * MavenProject}'s {@link MavenProject#getBuild() Build}'s 
   * {@link Build#getDirectory() getDirectory()} method.  Otherwise, it will
   * return the following:
   *
   * <pre>System.getProperty("maven.project.build.directory", 
   *                    System.getProperty("project.build.directory",
   *                                       String.format("%1$s%2$starget",
   *                                                     System.getProperty("basedir",
   *                                                                        System.getProperty("user.dir", ".")),
   *                                                     File.separator)));</pre>
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the current project's <i>build directory</i> name; never
   * {@code null}
   */
  public final String getProjectBuildDirectoryName() {
    String returnValue = null;
    final MavenProject project = this.getProject();
    if (project != null) {
      final Build build = project.getBuild();
      if (build != null) {
        final String buildDirectoryName = build.getDirectory();
        if (buildDirectoryName != null) {
          returnValue = buildDirectoryName;
        }
      }
    }
    if (returnValue == null) {
      returnValue = 
        System.getProperty("maven.project.build.directory", 
                           System.getProperty("project.build.directory",
                                              String.format("%1$s%2$starget",
                                                            System.getProperty("basedir",
                                                                               System.getProperty("user.dir", ".")),
                                                            File.separator)));
    }
    return returnValue;
  }

  /**
   * Initializes the {@link #getOutputFile() outputFile} property and
   * returns its value.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the newly-set value of the {@link #getOutputFile()
   * outputFile} property; never {@code null}
   *
   * @exception FileException if the {@link #getOutputFile()
   * outputFile} property could not be initialized
   */
  private final File initializeOutputFile() throws FileException {
    this.setOutputFile(this.initializeOutputFile(this.getOutputFile()));
    return this.getOutputFile();
  }

  /**
   * Validates and "absolutizes" the supplied {@link File} and returns
   * the corrected version.
   *
   * <p>The return value of this method is guaranteed to be a {@link
   * File} that is:</p>
   *
   * <ul>
   *
   * <li>non-{@code null}</li>
   *
   * <li>{@linkplain File#isAbsolute() absolute}</li>
   *
   * <li>existent and {@linkplain File#canWrite() writable} or
   * non-existent and {@linkplain File#getParentFile() parented} by a
   * directory that is existent and writable</li>
   *
   * </ul>
   *
   * <p>If the supplied {@link File} is a relative {@link File}, then
   * it will be made absolute by prepending it with the following
   * platform-neutral path: <tt>${{@link Build#getDirectory()
   * project.build.directory}}/generated-sources/jpa-maven-plugin/</tt></p>
   *
   * @param outputFile the {@link File} to validate
   *
   * @return the "absolutized" and validated value of the {@code
   * outputFile} parameter; never {@code null}
   *
   * @exception FileException if the supplied {@code outputFile} did
   * not pass validation
   */
  final File initializeOutputFile(File outputFile) throws FileException {
    if (outputFile == null) {
      final File projectBuildDirectory = new File(this.getProjectBuildDirectoryName());
      final File outputDirectory = new File(projectBuildDirectory, DEFAULT_SUBDIR_PREFIX);
      this.validateOutputDirectory(outputDirectory);
      outputFile = new File(outputDirectory, DEFAULT_OUTPUT_FILENAME);
    } else {
      if (!outputFile.isAbsolute()) {
        final File projectBuildDirectory = new File(this.getProjectBuildDirectoryName());
        final File outputDirectory = new File(projectBuildDirectory, DEFAULT_SUBDIR_PREFIX);
        this.validateOutputDirectory(outputDirectory);
        outputFile = new File(outputDirectory, outputFile.getPath());
      }
      if (outputFile.isDirectory()) {
        final File outputDirectory = outputFile;
        this.validateOutputDirectory(outputDirectory);
        outputFile = new File(outputDirectory, DEFAULT_OUTPUT_FILENAME);
      } else if (outputFile.exists()) {
        if (!outputFile.isFile()) {
          throw new NotNormalFileException(outputFile);
        } else if (!outputFile.canWrite()) {
          throw new NotWritableFileException(outputFile);
        } else {
          this.validateOutputDirectory(outputFile.getParentFile());
        }
      } else {
        this.validateOutputDirectory(outputFile.getParentFile());
      }
    }
    assert outputFile != null;
    assert outputFile.isAbsolute();
    final Log log = this.getLog();
    if (log != null && log.isDebugEnabled()) {
      log.debug(String.format("Output file initialized to %s", outputFile));
    }
    return outputFile;
  }
  
  /**
   * Ensures that the supplied {@link File}, after this method is
   * invoked, will designate a {@linkplain File#isDirectory()
   * directory} that {@linkplain File#mkdirs() exists} and is
   * {@linkplain File#canWrite() writable}.
   *
   * @param outputDirectory the {@link File} to validate; must not be
   * {@code null}
   *
   * @exception IllegalArgumentException if {@code outputDirectory} is
   * {@code null}
   *
   * @return {@code true} if {@link File#mkdirs()} was invoked on
   * {@code outputDirectory}; {@code false} otherwise
   *
   * @exception FileException if the supplied {@code outputDirectory}
   * failed validation
   */
  private boolean validateOutputDirectory(final File outputDirectory) throws FileException {
    boolean mkdirs = false;
    if (outputDirectory == null) {
      throw new IllegalArgumentException("outputDirectory", new NullPointerException("outputDirectory == null"));
    } else if (outputDirectory.exists()) {
      if (!outputDirectory.isDirectory()) {
        throw new NotDirectoryException(outputDirectory);
      }
      if (!outputDirectory.canWrite()) {
        throw new NotWritableDirectoryException(outputDirectory);
      }
    } else {
      mkdirs = outputDirectory.mkdirs();
      if (!mkdirs) {
        throw new PathCreationFailedException(outputDirectory);
      }
    }
    return mkdirs;
  }

  /**
   * Called by the {@link #execute()} method; initializes all fields
   * to their defaults if for some reason they were not already set
   * appropriately.
   *
   * <p>This method calls the following methods in order:
   *
   * <ol>
   *
   * <li>{@link #initializePropertyNames()}</li>
   *
   * <li>{@link #initializeURLs()}</li>
   *
   * <li>{@link #initializeOutputFile()}</li>
   *
   * </ol>
   *
   */
  private final void initialize() throws DependencyResolutionRequiredException, FileException {
    this.initializePropertyNames();
    this.initializeURLs();
    if (this.getUseOutputFile()) {
      this.initializeOutputFile();
    }
  }

  /**
   * Called by the {@link #initialize()} method; sets up the {@link
   * #propertyNames} field appropriately.
   */
  private final void initializePropertyNames() {
    if (this.propertyNames == null) {
      this.propertyNames = new HashMap<String, String>();
    }
    if (this.defaultPropertyName == null) {
      this.propertyNames.put(DEFAULT_DEFAULT_PROPERTY_NAME, "");
    } else {
      final String defaultPropertyName = this.defaultPropertyName.trim();
      if (defaultPropertyName.isEmpty()) {
        this.propertyNames.put(DEFAULT_DEFAULT_PROPERTY_NAME, "");
      } else {
        this.propertyNames.put(defaultPropertyName, "");
      }
    }
  }

  /**
   * Returns the encoding used to write the {@link Properties} file
   * that this mojo generates.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the encoding used to write the {@link Properties} file
   * that this mojo generates, or {@code null}
   */
  public String getEncoding() {
    return this.encoding;
  }

  /**
   * Sets the encoding used to write the {@link Properties} file that
   * this mojo generates.
   *
   * <p>If {@code null} is supplied to this method, then "{@code
   * UTF8}" will be used instead.</p>
   *
   * @param encoding the encoding to use; may be {@code null} in which
   * case "{@code UTF8}" will be used instead; otherwise the value is
   * {@linkplain String#trim() trimmed} and used as-is
   */
  public void setEncoding(String encoding) {
    if (encoding == null) {
      encoding = "";
    } else {
      encoding = encoding.trim();
    }
    if (encoding.isEmpty()) {
      this.encoding = "UTF8";
    } else {
      this.encoding = encoding;
    }
  }

  /**
   * Returns the output {@link File}.  This method does not perform
   * any validation or initialization.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the output {@link File}, or {@code null}
   *
   * @see #initializeOutputFile()
   */
  public File getOutputFile() {
    return this.outputFile;
  }

  /**
   * Sets the {@link File} to use as the output file parameter.  This
   * method does not perform any validation or initialization.
   *
   * @param file the {@link File} to use; may be {@code null}
   *
   * @see #initializeOutputFile()
   */
  public void setOutputFile(final File file) {
    this.outputFile = file;
  }

  /**
   * Returns whether or not this {@link ListEntityClassnamesMojo}
   * should write its properties out to the associated {@link
   * #getOutputFile() output file}.  By default this method returns
   * {@code true} for backwards compatibility.
   *
   * @return whether or not this {@link ListEntityClassnamesMojo}
   * should write its properties out to the associated {@link
   * #getOutputFile() output file}
   */
  public boolean getUseOutputFile() {
    return this.useOutputFile;
  }

  /**
   * Sets whether or not this {@link ListEntityClassnamesMojo} should
   * write its properties out to the associated {@linkplain
   * #getOutputFile() output file}.
   *
   * @param useOutputFile whether or not to use the associated
   * {@linkplain #getOutputFile() output file}
   */
  public void setUseOutputFile(final boolean useOutputFile) {
    this.useOutputFile = useOutputFile;
  }

  /**
   * Returns the {@link Set} of {@link URL}s to scan for annotations.
   * This method does not perform any validation or initialization.
   *
   * @return the {@link Set} of {@link URL}s to scan, or {@code null}
   *
   * @see #initializeURLs()
   */
  public Set<URL> getURLs() {
    return this.urls;
  }

  /**
   * Sets the {@link Set} of {@link URL}s to scan for annotations.
   * This method does not perform any validation or initialization.
   *
   * @param urls the {@link Set} of {@link URL}s to scan; may be
   * {@code null}
   *
   * @see #initializeURLs()
   */
  public void setURLs(final Set<URL> urls) {
    this.urls = urls;
  }

  /**
   * Scans the {@linkplain #getURLs() <tt>Set</tt> of <tt>URL</tt>s}
   * this {@link ListEntityClassnamesMojo} has been configured with
   * and returns the {@link AnnotationDB} that performed the scanning.
   *
   * <p>This method may return {@code null} in exceptional
   * circumstances.</p>
   *
   * @return an {@link AnnotationDB} containing the scan results, or
   * {@code null} if an {@linkplain #cloneAnnotationDB()
   * <tt>AnnotationDB</tt> could not be found}
   *
   * @exception MojoExecutionException if this mojo could not execute
   *
   * @exception MojoFailureExcetpion if the build should fail
   */
  private final AnnotationDB scan() throws IOException, MojoExecutionException, MojoFailureException {
    return this.scan(this.getURLs());
  }

  /**
   * Executes this mojo.
   *
   * @exception MojoExecutionException if this mojo could not be executed
   *
   * @exception MojoFailureException if the build should fail
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = this.getLog();
    if (log == null) {
      throw new MojoExecutionException("this.getLog() == null");
    }

    try {
      this.initialize();
    } catch (final DependencyResolutionRequiredException kaboom) {
      throw new MojoExecutionException(String.format("Dependencies of the current Maven project could not be downloaded during initialization of the jpa-maven-plugin."), kaboom);
    } catch (final NotWritableDirectoryException kaboom) {
      throw new MojoExecutionException(String.format("The output directory path, %s, exists and is a directory, but the current user, %s, cannot write to it.", kaboom.getFile(), System.getProperty("user.name")), kaboom);
    } catch (final NotWritableFileException kaboom) {
      throw new MojoExecutionException(String.format("The outputFile specified, %s, is a regular file, but cannot be written to by Maven running as user %s.  The outputFile parameter must designate either an existing, writable file or a non-existent file.", outputFile, System.getProperty("user.name")), kaboom);
    } catch (final NotNormalFileException kaboom) {
      throw new MojoExecutionException(String.format("The outputFile specified, %s, is not a directory, but is also not a normal file.  The outputFile parameter must deisgnate either an existing, writable, normal file or a non-existent file.", outputFile), kaboom);
    } catch (final NotDirectoryException kaboom) {
      throw new MojoExecutionException(String.format("The output directory path, %s, exists but is not a directory.", kaboom.getFile()), kaboom);
    } catch (final PathCreationFailedException kaboom) {
      throw new MojoExecutionException(String.format("Some portion of the output directory path, %s, could not be created.", kaboom.getFile()), kaboom);
    } catch (final FileException other) {
      throw new MojoExecutionException("An unexpected FileException occurred during initialization.", other);
    }

    // Scan the compile classpath for Entity, MappedSuperclass, IdClass,
    // Embeddable, etc. annotations.
    final AnnotationDB db;
    AnnotationDB tempDb = null;
    try {
      tempDb = this.scan();
    } catch (final IOException kaboom) {
      throw new MojoExecutionException("Execution failed because an IOException was encountered during URL scanning.", kaboom);
    } finally {
      db = tempDb;
      tempDb = null;
    }
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
        log.warn("After scanning for Entities, a null annotation index was returned by the AnnotationDB.");
      }
    } else if (ai.isEmpty()) {
      if (log.isWarnEnabled()) {
        log.warn("After scanning for Entities, no annotated Entities were found.");
      }
    } else {
      
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

      if (!entrySet.isEmpty()) {

        final String firstItemPrefix = this.getFirstItemPrefix();
        final String prefix = this.getPrefix();
        final String suffix = this.getSuffix();
        final String lastItemSuffix = this.getLastItemSuffix();

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
          assert !classNames.isEmpty();
          
          final Iterator<String> classNamesIterator = classNames.iterator();
          assert classNamesIterator != null;
          assert classNamesIterator.hasNext();
          
          while (classNamesIterator.hasNext()) {
            sb.append(this.decorate(classNamesIterator.next(), sb.length() <= 0 ? firstItemPrefix : prefix, classNamesIterator.hasNext() ? suffix : lastItemSuffix));
          }
          
          properties.setProperty(propertyName, sb.toString());
          
        }
      }

    }

    if (log.isDebugEnabled()) {
      final Enumeration<?> propertyNames = properties.propertyNames();
      if (propertyNames != null) {
        while (propertyNames.hasMoreElements()) {
          final Object nextElement = propertyNames.nextElement();
          if (nextElement != null) {
            final String key = nextElement.toString();
            assert key != null;
            final String value = properties.getProperty(key);
            log.debug(String.format("%s = %s", key, value));
          }
        }
      }
    }

    final MavenProject project = this.getProject();
    if (project != null) {
      final Properties projectProperties = project.getProperties();
      if (projectProperties != null) {
        @SuppressWarnings("unchecked")
        final Enumeration<String> propertyNames = (Enumeration<String>)properties.propertyNames();
        if (propertyNames != null && propertyNames.hasMoreElements()) {
          while (propertyNames.hasMoreElements()) {
            final String propertyName = propertyNames.nextElement();
            if (propertyName != null) {
              projectProperties.setProperty(propertyName, properties.getProperty(propertyName));
            }
          }
        }
      }
    }
    
    if (this.getUseOutputFile()) {
      final File outputFile = this.getOutputFile();
      if (outputFile != null) {
        assert outputFile.exists() ? outputFile.isFile() : true;
        assert outputFile.getParentFile() != null;
        assert outputFile.getParentFile().isDirectory();
        assert outputFile.getParentFile().canWrite();
        assert !outputFile.exists() ? outputFile.getParentFile().canWrite() : true;

        // Prepare to write.  Get the character encoding, accounting for
        // possible null return values from an overridden getEncoding()
        // method.
        String encoding = this.getEncoding();
        if (encoding == null) {
          encoding = "";
        } else {
          encoding = encoding.trim();
        }
        if (encoding.isEmpty()) {
          encoding = "UTF8";
        }

        // Set up the Writer to point to the outputFile and have the
        // Properties store itself there.
        Writer writer = null;
        try {
          writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding));
          properties.store(writer, "Generated by " + this.getClass().getName());
          writer.flush();
        } catch (final IOException kaboom) {
          throw new MojoExecutionException(String.format("While attempting to write to the outputFile parameter (%s), an IOException was encountered.", outputFile), kaboom);
        } finally {
          if (writer != null) {
            try {
              writer.close();
            } catch (final IOException ignore) {
              // ignored on purpose
            }
          }
        }
      }
    }
  }

  /**
   * Returns the property name to use for the names of {@link Class}es
   * that belong to the default package.
   *
   * <p>This method may return {@code null}.  However, other portions
   * of this mojo's code may substitute a default value in such
   * cases.</p>
   *
   * @return the property name to use for the names of {@link Class}es
   * that belong to the default package, or {@code null}
   */
  public String getDefaultPropertyName() {
    return this.defaultPropertyName;
  }

  /**
   * Sets the property name to use for the names of {@link Class}es
   * that belong to the default package.
   *
   * @param defaultPropertyName the property name; may be {@code
   * null}, but this mojo may use a default value instead
   */
  public void setDefaultPropertyName(String defaultPropertyName) {
    if (defaultPropertyName == null) {
      defaultPropertyName = "";
    } else {
      defaultPropertyName = defaultPropertyName.trim();
    }
    if (defaultPropertyName.isEmpty()) {
      defaultPropertyName = DEFAULT_DEFAULT_PROPERTY_NAME;
    }
    this.defaultPropertyName = defaultPropertyName;
  }

  /**
   * Returns the appropriate property name given a {@linkplain Class#getName() class name}.
   *
   * <p>If the supplied {@code className} is {@code null} or consists
   * solely of {@linkplain Character#isWhitespace(char) whitespace},
   * then the {@linkplain #getDefaultPropertyName() default property
   * name} is returned.<p>
   *
   * <p>Otherwise, a property name is 
   */
  public String determinePropertyName(String className) {
    final Log log = this.getLog();
    assert log != null;
    String propertyName = this.getDefaultPropertyName();
    if (className != null) {
      className = className.trim();
      if (!className.isEmpty()) {
        
        // Find the class' package name.  Extract "com.foobar" from
        // "com.foobar.Foo".
        final int index = Math.max(0, className.lastIndexOf('.'));
        String packageName = className.substring(0, index);
        assert packageName != null;
        if (log.isDebugEnabled()) {
          log.debug("Package: " + packageName);
        }
        
        final Map<String, String> propertyNames = this.getPropertyNames();
        if (propertyNames == null) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("Property names were never initialized; assigning default property name (%s) to class name %s.", propertyName, className));
          }
        } else if (propertyNames.isEmpty()) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("Property names were initialized to the empty set; assigning default property name (%s) to class name %s.", propertyName, className));
          }
        } else {
          propertyName = propertyNames.get(packageName);
          while (propertyName == null && packageName != null && !packageName.isEmpty()) {
            final int dotIndex = Math.max(0, packageName.lastIndexOf('.'));
            packageName = packageName.substring(0, dotIndex);
            if (log.isDebugEnabled()) {
              log.debug("Package: " + packageName);
            }
            propertyName = propertyNames.get(packageName);
          }
        }
      }
    }
    if (propertyName == null) {
      propertyName = this.getDefaultPropertyName();
      if (propertyName == null) {
        propertyName = DEFAULT_DEFAULT_PROPERTY_NAME;
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("propertyName: " + propertyName);
    }
    return propertyName;
  }

  /**
   * Decorates the supplied {@link Class#getName() class name} with
   * the supplied {@code prefix} and {@code suffix} parameters and
   * returns the result.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param classname the class name to decorate; if {@code null} then
   * {@code null} will be returned
   *
   * @param prefix the prefix to decorate with; may be {@code null}
   *
   * @param suffix the suffix to decorate with; may be {@code null}
   *
   * @return the decorated class name, or {@code null}
   */
  protected String decorate(final String classname,
                            final String prefix,
                            final String suffix) {
    final String returnValue;
    if (classname == null) {
      returnValue = null;
    } else {
      final StringBuilder sb = new StringBuilder();     
      if (prefix != null) {
        sb.append(prefix); 
      }
      sb.append(classname);
      if (suffix != null) {
        sb.append(suffix);
      }
      returnValue = sb.toString();
    }
    return returnValue;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation overrides that of {@link AbstractJPAMojo}
   * to ensure that the created {@link AnnotationDB} {@linkplain
   * AnnotationDB#setScanClassAnnotations(boolean) only scans
   * <tt>Class</tt>-level annotations}.</p>
   *
   * @return {@inheritDoc}
   */
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
