/*****************************************************************
   Copyright 2006 by Tung Luong (lqtung@truthinet.com)

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
package com.inet.base.ejb.search;

import java.io.Serializable;

/**
 * BaseSearchBean.
 *
 * @author <a href="mailto:lqtung@truthinet.com">Tung Luong</a>
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BaseSearchBean.java 2009-07-30 18:56:24z nguyen_dv $
 *
 * Create date: Jul 30, 2009
 * <pre>
 *  Initialization BaseSearchBean class.
 * </pre>
 */
public abstract class BaseSearchBean implements Serializable {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 2757566000032254014L;

  /* the number of records will be return. */
  private int limit;

  /* start position. */
  private int offset;

  /* the first time will query to get total result. */
  private boolean firstTime;

  /* total result according search criteria. */
  private int total;

  /**
   * Default constructor.
   */
  public BaseSearchBean() {
    limit = Integer.MAX_VALUE;
    offset = 0;
    firstTime = true;
  }

  /**
   * Returns the limited items return to client.
   *
   * @return the limited items return to client.
   */
  public int getLimit() {
    return this.limit;
  }

  /**
   * Set the limited item return to client.
   *
   * @param l the given limited item return to client to set.
   */
  public void setLimit(final int l) {
    this.limit = l;
  }

  /**
   * Returns the start position where to get result.
   *
   * @return the start position where to get result.
   */
  public int getOffset() {
    return this.offset;
  }

  /**
   * Set the start position where to get result.
   *
   * @param o the given start position where to get result to set.
   */
  public void setOffset(final int o) {
    this.offset = o;
  }

  /**
   * Returns if this is the first time to get result.
   *
   * @return the this is the first time to get result.
   */
  public boolean isFirstTimes() {
    return this.firstTime;
  }

  /**
   * Set the first time value to get the result.
   *
   * @param first the given first time value to set.
   */
  public void setFirstTimes(final boolean first) {
    this.firstTime = first;
  }

  /**
   * Returns the total items that matched query.
   *
   * @return the total items that matched query.
   */
  public int getTotal() {
    return this.total;
  }

  /**
   * Set the total items that matched query.
   *
   * @param t the given total items that matched query value to set.
   */
  public void setTotal(final int t) {
    this.total = t;
  }
}
