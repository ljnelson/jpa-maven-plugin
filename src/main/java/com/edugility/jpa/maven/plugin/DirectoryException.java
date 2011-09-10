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

import java.io.File;

/**
 * A {@link FileException} that results from a {@link File}
 * representing a directory failing validation of some kind.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0-SNAPSHOT
 *
 * @since 1.0-SNAPSHOT
 *
 * @see FileException
 */
public abstract class DirectoryException extends FileException {

  /**
   * Creates a new {@link DirectoryException}.
   *
   * @param directory the {@link File} whose validation failure caused
   * this {@link DirectoryException} to be thrown; may be {@code null}
   */
  protected DirectoryException(final File directory) {
    super(directory);
  }

  /**
   * Creates a new {@link DirectoryException}.
   *
   * @param directory the {@link File} whose validation failure caused
   * this {@link DirectoryException} to be thrown; may be {@code null}
   *
   * @param message a detail message further explaining this {@link
   * DirectoryException}; may be {@code null}
   */
  protected DirectoryException(final File directory, final String message) {
    super(directory, message);
  }

  /**
   * Creates a new {@link DirectoryException}.
   *
   * @param directory the {@link File} whose validation failure caused
   * this {@link DirectoryException} to be thrown; may be {@code null}
   *
   * @param cause the {@link Throwable} that contributed to this
   * {@link DirectoryException}'s cause; may be {@code null}
   *
   * @param message a detail message further explaining this {@link
   * DirectoryException}; may be {@code null}
   */
  protected DirectoryException(final File directory, final Throwable cause, final String message) {
    super(directory, cause, message);
  }

  /**
   * A convenience method that returns the return value of the {@link
   * #getFile()} method.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the result of calling the {@link #getFile()} method, or
   * {@code null}
   */
  public final File getDirectory() {
    return this.getFile();
  }

}