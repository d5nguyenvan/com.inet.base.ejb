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
package com.inet.base.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inet.base.ejb.conf.ConfigBean;
import com.inet.base.ejb.internal.StringUtils;

/**
 * ServiceLocator.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: ServiceLocator.java 2007-12-31 19:13:06z nguyen_dv $
 *
 * @since 1.0
 */
public final class ServiceLocator {
  /** class logger. */
  private static final Logger LOG = LoggerFactory.getLogger(ServiceLocator.class);

  /** EJB Context. */
  private Context context = null;

  /** remote resource. */
  private static final String REMOTE_NAME = "/remote";

  /** local resource. */
  private static final String LOCAL_NAME = "/local";

  /**
   * ServiceLocator constructor.
   */
  ServiceLocator() {
    try {
      context = new InitialContext();
    } catch (final NamingException nex) {
      LOG.warn("Could not initialize the bean context.");
    }
  }

  /**
   * ServiceLocator constructor.
   *
   * @param enviroments the given environment used to initialize the context.
   */
  ServiceLocator(final Properties enviroments) {
    try {
      context = new InitialContext(enviroments);
    } catch (final NamingException nex) {
      LOG.warn("Could not initialize the bean context from the given enviroment.");
    }
  }

  /**
   * Get remote bean.
   * @param <T> the given bean type.
   *
   * @param beanName the given bean name.
   * @param clazz the given bean type.
   * @return the bean instance.
   */
  public <T> T getRemoteBean(final String beanName, final Class<T> clazz) {
    // get remote bean.
    if (context == null || clazz == null) {
      return null;
    }

    // initialize the new bean.
    try {
      final Object beanObj = context.lookup(getFullBeanPath(beanName + REMOTE_NAME));

      return clazz.cast(beanObj);
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the remote bean with name {}.", beanName);
    } catch (final ClassCastException ccex) {
      LOG.warn("Could not cast bean to class {}.", clazz.getName());
    }

    return null;
  }

  /**
   * Get remote bean.
   *
   * @param beanName the given remote bean name.
   * @return the remote bean instance.
   */
  public Object getRemoteBean(final String beanName) {
    if (context == null) {
      return null;
    }

    try {
      final Object beanObj = context.lookup(getFullBeanPath(beanName + REMOTE_NAME));
      return beanObj;
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the remote bean with name {}.", beanName);
    }

    return null;
  }

  /**
   * Get local bean.
   * @param <T> the given bean type.
   *
   * @param beanName the given local bean name.
   * @param clazz the given local bean type.
   * @return the local bean instance.
   */
  public <T> T getBean(final String beanName, final Class<T> clazz) {
    if (context == null || clazz == null) {
      return null;
    }

    try {
      Object beanObj = context.lookup(getFullBeanPath(beanName + LOCAL_NAME));
      return clazz.cast(beanObj);
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the local bean with name {}.", beanName);
    } catch (final ClassCastException ccex) {
      LOG.warn("Could not cast bean to class {}.", clazz.getName());
    }

    return null;
  }

  /**
   * Lookup the given bean name and reference.
   * @param <T> the given bean type.
   *
   * @param beanName the given bean name.
   * @param reference the given reference name (local,remote).
   * @param clazz the given bean type.
   * @return the bean instance.
   */
  public <T> T lookup(final String beanName, final String reference, final Class<T> clazz) {
    if (context == null || clazz == null) {
      return null;
    }

    try {
      Object beanObj = context.lookup(getFullBeanPath(beanName + '/' + reference));
      return clazz.cast(beanObj);
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the bean with name {} and reference {}.", beanName, reference);
    } catch (final ClassCastException ccex) {
      LOG.warn("Could not cast bean to class {}.", clazz.getName());
    }
    return null;
  }

  /**
   * Lookup the given bean name and reference.
   * @param <T> the given bean type.
   *
   * @param beanName the given bean name.
   * @param reference the given reference name (local,remote).
   * @return the bean instance.
   */
  @SuppressWarnings("unchecked")
  public <T> T lookup(final String beanName, final String reference) {
    if (context == null) {
      return null;
    }

    try {
      final Object beanObj = context.lookup(getFullBeanPath(beanName + '/' + reference));
      return (T) beanObj;
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the bean with name {} and reference {}.", beanName, reference);
    }

    return null;
  }

  /**
   * Lookup the bean with the given bean name.
   * @param <T> the given bean type.
   *
   * @param name the given bean name.
   * @return the bean reference instance; may be {@code null}.
   */
  public <T> T lookup(final String name, final Class<T> clazz) {
    if (context == null || clazz == null) {
      return null;
    }

    try {
      final Object bean = context.lookup(getFullBeanPath(name));
      return clazz.cast(bean);
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the bean with name {}.", name);
    } catch (final ClassCastException ccex) {
      LOG.warn("Could not cast bean to class {}.", clazz.getName());
    }

    return null;
  }

  /**
   * Lookup the bean with the given bean name.
   * @param <T> the given bean type.
   *
   * @param name the given bean name.
   * @return the bean reference instance; may be {@code null}.
   */
  @SuppressWarnings({"unchecked"})
  public <T> T lookup(final String name) {
    if (context == null) {
      return null;
    }

    try {
      final Object bean = context.lookup(getFullBeanPath(name));
      return (T) bean;
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the bean with name {}.", name);
    }

    return null;
  }

  /**
   * Get local bean.
   *
   * @param beanName the given local bean name.
   * @return the local bean instance.
   */
  public Object getBean(final String beanName) {
    if (context == null) {
      return null;
    }

    try {
      final Object beanObj = context.lookup(getFullBeanPath(beanName + LOCAL_NAME));
      return beanObj;
    } catch (final NamingException nex) {
      LOG.warn("Could not lookup the local bean with name {}.", beanName);
    }

    return null;
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

  /**
   * Dispose object.
   */
  public synchronized void dispose() {
    try {
      if (context != null) {
        context.close();
      }
    } catch (final NamingException nex) {
      LOG.warn("Could not dispose the bean context.");
    }
  }
}
