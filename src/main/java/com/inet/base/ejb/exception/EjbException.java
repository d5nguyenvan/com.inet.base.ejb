/*****************************************************************
d   Copyright 2006 by Dung Nguyen (dungnguyen@truthinet.com)

   Licensed under the iNet Solutions Corp.,;
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.truthinet.com/licenses

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*****************************************************************/
package com.inet.base.ejb.exception;

/**
 * EjbException.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: EjbException.java 2007-12-31 17:05:04z nguyen_dv $
 */
public class EjbException extends RuntimeException {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -371030325089916939L;

  /**
   * Default exception.
   */
  public EjbException() {
    // do nothing as of yet.
  }

  /**
   * Attaches the message to exception.
   *
   * @param msg the given exception message.
   */
  public EjbException(final String msg) {
    super(msg);
  }

  /**
   * Attaches the given throwable to exception.
   *
   * @param throwable the given {@link Throwable throwable} instance attached into exception
   * message.
   */
  public EjbException(final Throwable throwable) {
    super(throwable);
  }

  /**
   * Attaches the given message and throwable to exception.
   *
   * @param msg the given exception message.
   * @param throwable the given {@link Throwable throwable} instance attached into exception
   * message.
   */
  public EjbException(final String msg, final Throwable throwable) {
    super(msg, throwable);
  }
}
