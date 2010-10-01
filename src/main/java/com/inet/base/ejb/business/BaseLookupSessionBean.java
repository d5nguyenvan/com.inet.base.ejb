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
package com.inet.base.ejb.business;

import java.util.Properties;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inet.base.ejb.conf.ConfigBean;
import com.inet.base.ejb.internal.StringUtils;

/**
 * BaseLookupSessionBean.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BaseLookupSessionBean.java 2007-12-31 10:33:34z nguyen_dv $
 *
 * @since 1.0
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BaseLookupSessionBean {
  //~ static fields =========================================================
  /**
   * class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BaseLookupSessionBean.class);

  /**
   * the remove bean reference.
   */
  protected static final String REMOTE = "/remote";

  /**
   * the local bean reference.
   */
  protected static final String LOCAL = "/local";

  //~ instance fields =======================================================
  /**
   * the lookup context.
   */
  protected Context context;

  /**
   * Constructor.
   *
   * @param environment the given {@link Properties initialize environment} parameters.
   */
  public BaseLookupSessionBean(final Properties environment) {
    // Has environment parameter.
    try {
      if (environment != null) {
        // initial context from the given environment.
        context = new InitialContext(environment);
      } else {
        // initial context from the default environment.
        context = new InitialContext();
      }
    } catch (final NamingException nex) {
      // log warning.
      LOG.warn("Could not initalization the lookup context.");
    }
  }

  /**
   * Looks up the remote bean from the bean name and the type of bean.
   * @param <T> the remote bean type.
   *
   * @param beanName the given bean name.
   * @param clazz the type of bean name.
   * @return the remote bean.
   */
  protected <T> T lookupRemote(final String beanName, final Class<T> clazz) {
    if (beanName != null) {
      try {
        // lookup remote bean object.
        final Object remoteObject = context.lookup(getFullBeanPath(beanName + REMOTE));
        if (LOG.isInfoEnabled()) {
          LOG.info("Lookup the remote bean object [{}] with name [{}].", new Object[]{ remoteObject, beanName });
        }

        // cast and return this bean.
        return clazz.cast(remoteObject);
      } catch (final NamingException nex) {
        LOG.warn("Could not lookup the bean remote bean [{}].", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Looks up the local bean from the given bean name and the type of bean.
   * @param <T> the local bean type.
   *
   * @param beanName the given bean name.
   * @param clazz the type of bean name.
   * @return the local bean instance.
   */
  protected <T> T lookupLocal(final String beanName, final Class<T> clazz) {
    if (beanName != null) {
      try {
        // lookup local bean.
        final Object localObject = context.lookup(getFullBeanPath(beanName + LOCAL));
        if (LOG.isInfoEnabled()) {
          LOG.info("Lookup the bean object [{}] with name [{}].", new Object[]{ localObject, beanName });
        }

        // cast and return this bean.
        return clazz.cast(localObject);
      } catch (final NamingException nex) {
        LOG.warn("Could not lookup the local bean [{}].", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Returns the current {@link Context context} instance.
   *
   * @return the current {@link Context context} instance.
   */
  public Context getContext() {
    return context;
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
