/*****************************************************************
   Copyright 2006 by Dung Nguyen (dungnguyen@truthinet.com)

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
package com.inet.base.ejb.persistence;

import java.io.Serializable;

/**
 * BasePersistence.
 * @param <T> persistence primary key type.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BasePersistence.java 2007-12-31 17:16:21z nguyen_dv $
 */
public abstract class BasePersistence<T> implements Serializable {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -4300497175382390878L;

  /** the persistence primary key. */
  protected T id;

  /**
   * Set persistence primary key.
   *
   * @param identifier the given persistence primary key value to set.
   */
  public void setId(final T identifier) {
    this.id = identifier;
  }
}
