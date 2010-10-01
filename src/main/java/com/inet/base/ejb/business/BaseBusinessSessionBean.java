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
package com.inet.base.ejb.business;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inet.base.ejb.conf.ConfigBean;
import com.inet.base.ejb.internal.StringUtils;

/**
 * BaseBusinessSessionBean.
 * <pre>
 *  Initialization BaseBusinessSessionBean class.
 *  The base business session bean, inject session context and entity manager.
 * </pre>
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BaseBusinessSessionBean.java 2007-12-31 09:56:08z nguyen_dv $
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public abstract class BaseBusinessSessionBean {
  //~ static fields =========================================================
  /**
   * class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BaseBusinessSessionBean.class);

  /**
   * the remote bean reference.
   */
  protected static final String REMOTE = "/remote";

  /**
   * the local bean reference.
   */
  protected static final String LOCAL = "/local";

  //~ instance fields =======================================================
  /**
   * session context.
   */
  protected SessionContext sessionContext;

  /**
   * Set {@link SessionContext session context}.
   *
   * @param sc the given {@link SessionContext session context} to be set.
   */
  @Resource
  public void setSessionContext(final SessionContext sc) {
    this.sessionContext = sc;
  }

  /**
   * Returns the current {@link SessionContext session context} instance.
   *
   * @return the current {@link SessionContext session context} instance.
   */
  public SessionContext getSessionContext() {
    return sessionContext;
  }

  /**
   * Looks up the remote bean from the bean name and the type of bean.
   * @param <V> the bean type.
   *
   * @param beanName the given bean name.
   * @param clazz the type of bean name.
   * @return the remote bean instance.
   */
  protected <V> V lookupRemote(final String beanName, final Class<V> clazz) {
    if (beanName != null) {
      try {
        // lookup remote bean object.
        final Object remoteObject = sessionContext.lookup(getFullBeanPath(beanName + REMOTE));
        if (LOG.isInfoEnabled()) {
          LOG.info("Lookup the remote bean object [{}] with name [{}].", new Object[]{ remoteObject, beanName });
        }

        // cast and return this bean.
        return clazz.cast(remoteObject);
      } catch (final Exception ex) {
        LOG.error("Could not lookup the remote bean {}.", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Looks up the local bean from the given bean name and the type of bean.
   * @param <V> the bean type.
   *
   * @param beanName the given bean name.
   * @param clazz the type of bean name.
   * @return the local bean instance.
   */
  protected <V> V lookupLocal(final String beanName, final Class<V> clazz) {
    if (beanName != null) {
      try {
        // lookup local bean.
        final Object localObject = sessionContext.lookup(getFullBeanPath(beanName + LOCAL));
        if (LOG.isInfoEnabled()) {
          LOG.info("Lookup the bean object [{}] with name [{}].", new Object[]{ localObject, beanName });
        }

        // cast and return this bean.
        return clazz.cast(localObject);
      } catch (final Exception ex) {
        LOG.error("Could not lookup the local bean {}", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  //~ helper functions ======================================================
  /**
   * @return the empty name when user does not.
   */
  protected String getUsername() {
    try {
      // get caller principal name.
      final String login = sessionContext.getCallerPrincipal().getName();
      if (LOG.isInfoEnabled()) {
        LOG.info("Current login name [{}].", login);
      }

      // check the login name and return the right value.
      return (!StringUtils.isset(login) ? StringUtils.EMPTY_STRING : login);
    } catch (final Exception ex) {
      LOG.warn("Could not get the current login name");
    }

    // any ways return the blank value.
    return StringUtils.EMPTY_STRING;
  }

  /**
   * Returns the full bean path from the relative bean path.
   *
   * @param beanPath the given relative bean path.
   * @return the full bean path.
   */
  private String getFullBeanPath(final String beanPath) {
    // get app path.
    final String appPath = (ConfigBean.getInstance() != null
                              ? ConfigBean.getInstance().getAppLookup()
                              : null);
    if (!StringUtils.hasLength(appPath) || beanPath.startsWith(appPath)) {
      return beanPath;
    }
    return (appPath.endsWith("/") ? (appPath + beanPath) : (appPath + '/' + beanPath));
  }
}
