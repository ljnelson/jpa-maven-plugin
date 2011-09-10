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

import java.net.URL;

import java.util.Collection; // for javadoc only
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

 /**
  * A filter that can cull a {@link Collection} of {@link URL}s.
  *
  * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
  *
  * @version 1.0-SNAPSHOT
  *
  * @since 1.0-SNAPSHOT
  */
public class URLFilter {
  
  /**
   * The {@linkplain FileUtils Plexus <tt>FileUtils</tt>-compatible}
   * {@link Set} of inclusion {@link String}s.
   *
   * <p>This field may be {@code null} at any point.</p>
   */
  private Set<String> includes;

  /**
   * The {@linkplain FileUtils Plexus <tt>FileUtils</tt>-compatible}
   * {@link Set} of exclusion {@link String}s.
   *
   * <p>This field may be {@code null} at any point.</p>
   */
  private Set<String> excludes;
  
  /**
   * Creates a new {@link URLFilter}.
   */
  public URLFilter() {
    super();
  }

  /**
   * Returns {@code true} if the supplied {@link URL} should be
   * accepted.  This implementation returns {@code true} if the
   * supplied {@link URL} is non-{@code null}.
   *
   * <p><strong>Note:</strong> inclusion and exclusion does not yet
   * work.</p>
   *
   * @param url the {@link URL} to accept; may be {@code null}
   *
   * @return {@code true} if the supplied {@link URL} should be
   * accepted; {@code false} otherwise
   */
  public boolean accept(final URL url) {
    if (url == null) {
      return false;
    }
    return true;
  }

  /**
   * Sets the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of inclusion {@link
   * String}s.
   *
   * @param includes the {@link Set} of {@link String}s to set; may be
   * {@code null}
   */
  public void setIncludes(final Set<String> includes) {
    this.includes = includes;
  }

  /**
   * Returns the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of inclusion {@link
   * String}s.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of inclusion {@link
   * String}s, or {@code null}
   */
  public Set<String> getIncludes() {
    return this.includes;
  }

  /**
   * Sets the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of exclusion {@link
   * String}s.
   *
   * @param excludes the {@link Set} of {@link String}s to set; may be
   * {@code null}
   */
  public void setExcludes(final Set<String> excludes) {
    this.excludes = excludes;
  }

  /**
   * Returns the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of exclusion {@link
   * String}s.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@linkplain FileUtils Plexus
   * <tt>FileUtils</tt>-compatible} {@link Set} of exclusion {@link
   * String}s, or {@code null}
   */
  public Set<String> getExcludes() {
    return this.excludes;
  }

}