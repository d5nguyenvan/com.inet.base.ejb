/*****************************************************************
 Copyright 2006 by Tin Vo (tinvo@truthinet.com)

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
import java.util.List;

/**
 * SearchWrapper.
 *
 * @author <a href="mailto:lqtung@truthinet.com">Tung Luong</a>
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: SearchWrapper.java 2009-07-30 21:10:34z nguyen_dv $
 *
 * Create date: Jul 30, 2009
 * <pre>
 *  Initialization SearchWrapper class.
 * </pre>
 */
public class SearchWrapper<T> implements Serializable {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 1353992346897709580L;

  /* contain one page of result, T is must serial. */
  private List<T> list;

  /* total result according search criteria */
  private int total;

  /**
   * SearchWrapper constructor.
   */
  public SearchWrapper() {
    super();
  }

  /**
   * Returns the {@link java.util.List list} of result.
   *
   * @return the {@link java.util.List list} of result.
   */
  public List<T> getList() {
    return list;
  }

  /**
   * Set the {@link java.util.List list} of result.
   *
   * @param l the given {@link java.util.List list} of result to set.
   */
  public void setList(final List<T> l) {
    this.list = l;
  }

  /**
   * Returns the total of result.
   *
   * @return the total of result.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Set the total of result.
   *
   * @param t the given total of result.
   */
  public void setTotal(final int t) {
    this.total = t;
  }
}