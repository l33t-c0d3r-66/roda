/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RODAException extends Exception {
  private static final long serialVersionUID = -1656013708463576500L;

  public RODAException() {
    super();
  }

  public RODAException(String message) {
    super(message);
  }

  public RODAException(String message, Throwable cause) {
    super(message, cause);
  }

  public RODAException(Throwable cause) {
    super(cause);
  }

}
