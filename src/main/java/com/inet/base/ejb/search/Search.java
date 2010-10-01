/*****************************************************************
   Copyright 2006 by Tung Luong (lqtung@truthinet.com.vn)

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

import org.hibernate.Criteria;

import com.inet.base.ejb.exception.EjbException;

/**
 * Search.
 *
 * @author <a href="mailto:lqtung@truthinet.com.vn">Tung Luong</a>
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: Search.java 2009-07-30 19:30:03z nguyen_dv $
 */
public interface Search {
  /**
   * Create criteria search or count criteria.
   *
   * @param count if {@code true} will create count criteria, otherwise create search criteria.
   * @return the search/count {@link org.hibernate.Criteria criteria}.
   * @throws EjbException if could not create criteria instance.
   */
  Criteria createCriteria(boolean count) throws EjbException;
}
