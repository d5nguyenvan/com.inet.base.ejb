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

import com.inet.base.ejb.internal.StringUtils;
import com.inet.base.ejb.internal.ref.SoftHashMap;

/**
 * ServiceLocatorManager.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: ServiceLocatorManager.java 2009-12-31 20:09:44z nguyen_dv $
 *
 * @since 1.0
 */
public final class ServiceLocatorManager {
  /** default properties. */
  private static final Properties DEFAULT_PROPERTIES = new Properties();

  /**
   * Default JBOSS factory PKG.
   */
  private static final String FACTORY_PKG = "org.jboss.naming:org.jnp.interfaces";

  /**
   * Default JBOSS factory.
   */
  private static final String FACTORY = "org.jnp.interfaces.NamingContextFactory";

  /** initialize service locator. */
  private SoftHashMap<Properties, ServiceLocator> serviceLocators
    = new SoftHashMap<Properties, ServiceLocator>();

  /** cache properties. */
  private SoftHashMap<String, Properties> properties
    = new SoftHashMap<String, Properties>();

  /** Initialize the locator manager. */
  private static final ServiceLocatorManager LOCATOR_MANAGER = new ServiceLocatorManager();

  /**
   * Create ServiceLocatorManager instance.
   */
  private ServiceLocatorManager() {
    // do nothing as of yet
  }

  /**
   * Get service locator manager.
   *
   * @return {@link ServiceLocatorManager service locator manager}
   */
  public static ServiceLocatorManager getInstance() {
    return LOCATOR_MANAGER;
  }

  /**
   * Get service locator using default environment.
   *
   * @return {@link ServiceLocator service locator}
   */
  public ServiceLocator getServiceLocator() {
    // get the service locator.
    synchronized (this.serviceLocators) {
      if (!this.serviceLocators.containsKey(DEFAULT_PROPERTIES)) {
        final ServiceLocator serviceLocator = new ServiceLocator();
        this.serviceLocators.put(DEFAULT_PROPERTIES, serviceLocator);
      }
    }

    // return the service locator.
    return this.serviceLocators.get(DEFAULT_PROPERTIES);
  }

  /**
   * Get service locator.
   *
   * @param enviroment the given {@link Properties properties} to initialize service locator.
   * @return {@link ServiceLocator service locator} instance
   */
  public ServiceLocator getServiceLocator(final Properties enviroment) {
    // get the service locator.
    synchronized (this.serviceLocators) {
      if (!this.serviceLocators.containsKey(enviroment)) {
        final ServiceLocator serviceLocator = new ServiceLocator(enviroment);
        this.serviceLocators.put(enviroment, serviceLocator);
      }
    }

    // return the service locator.
    return this.serviceLocators.get(enviroment);
  }

  /**
   * Get service locator from the given host and port information.
   *
   * @param host the given host information.
   * @param port the given port information.
   * @return the {@link ServiceLocator service locator} instance.
   */
  public ServiceLocator getServiceLocator(final String host, final String port) {
    // get the properties from the given host and port.
    final String providerUrl = combineKey(host, port);

    // create environment if does not exist.
    synchronized (this.properties) {
      if (!this.properties.containsKey(providerUrl)) {
        // create environment.
        final Properties env = new Properties();

        // setting data.
        env.put(Context.INITIAL_CONTEXT_FACTORY, ServiceLocatorManager.FACTORY);
        env.put(Context.URL_PKG_PREFIXES, ServiceLocatorManager.FACTORY_PKG);
        env.put(Context.PROVIDER_URL, providerUrl);

        // put to properties.
        this.properties.put(providerUrl, env);
      }
    }

    // creates ServiceLocator if does not exist.
    final Properties environment = this.properties.get(providerUrl);
    synchronized (this.serviceLocators) {
      // create service locator.
      final ServiceLocator serviceLocator = new ServiceLocator(environment);

      // put to data.
      this.serviceLocators.put(environment, serviceLocator);
    }

    // return the service locator.
    return this.serviceLocators.get(environment);
  }

  /**
   * Get service locator from the given host and port information.
   *
   * @param factory the given factory information.
   * @param host the given host information.
   * @param port the given port information.
   * @return the {@link ServiceLocator service locator} instance.
   */
  public ServiceLocator getServiceLocator(final String factory, final String host,
      final String port) {
    // get the properties from the given host and port.
    final String providerUrl = combineKey(host, port);

    // the factory key.
    final String key = combineKey(factory, host, port);

    // create environment if does not exist.
    synchronized (this.properties) {
      if (!this.properties.containsKey(key)) {
        // create environment.
        final Properties env = new Properties();

        // setting data.
        env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        env.put(Context.PROVIDER_URL, providerUrl);

        // put to properties.
        this.properties.put(key, env);
      }
    }

    // creates ServiceLocator if does not exist.
    final Properties environment = this.properties.get(key);
    synchronized (this.serviceLocators) {
      // create service locator.
      final ServiceLocator serviceLocator = new ServiceLocator(environment);

      // put to data.
      this.serviceLocators.put(environment, serviceLocator);
    }

    // return the service locator.
    return this.serviceLocators.get(environment);
  }

  /**
   * Get service locator from the given host and port information.
   *
   * @param factory the given factory information.
   * @param pkgPrefix the given PKG prefix information.
   * @param host the given host information.
   * @param port the given port information.
   * @return the {@link ServiceLocator service locator} instance.
   */
  public ServiceLocator getServiceLocator(final String factory, final String pkgPrefix,
      final String host, final String port) {
    // get the properties from the given host and port.
    final String providerUrl = combineKey(host, port);

    // the factory key.
    final String key = combineKey(factory, pkgPrefix, host, port);

    // create environment if does not exist.
    synchronized (this.properties) {
      if (!this.properties.containsKey(key)) {
        // create environment.
        final Properties env = new Properties();

        // setting data.
        env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        env.put(Context.URL_PKG_PREFIXES, pkgPrefix);
        env.put(Context.PROVIDER_URL, providerUrl);

        // put to properties.
        this.properties.put(key, env);
      }
    }

    // creates ServiceLocator if does not exist.
    final Properties environment = this.properties.get(key);
    synchronized (this.serviceLocators) {
      // create service locator.
      final ServiceLocator serviceLocator = new ServiceLocator(environment);

      // put to data.
      this.serviceLocators.put(environment, serviceLocator);
    }

    // return the service locator.
    return this.serviceLocators.get(environment);
  }

  /**
   * Remove service locator using default environment.
   */
  public void removeServiceLocator() {
    // remove the service locator.
    synchronized (this.serviceLocators) {
      final ServiceLocator serviceLocator = this.serviceLocators.remove(DEFAULT_PROPERTIES);
      serviceLocator.dispose();
    }
  }

  /**
   * Remove service locator.
   *
   * @param environment the given {@link Properties environment} information.
   */
  public void removeServiceLocator(final Properties environment) {
    // remove the service locator.
    synchronized (this.serviceLocators) {
      if (this.serviceLocators.containsKey(environment)) {
        final ServiceLocator serviceLocator = this.serviceLocators.remove(environment);
        serviceLocator.dispose();
      }
    }
  }

  /**
   * Remove the service locator from the given host and port.
   *
   * @param host the given host information.
   * @param port the given port information.
   */
  public void removeServiceLocator(final String host, final String port) {
    // create the key.
    final String key = combineKey(host, port);

    // remove the properties.
    synchronized (this.properties) {
      // remove the service locator.
      if (this.properties.containsKey(key)) {
        // get the properties.
        final Properties environment = this.properties.remove(key);

        // get service locator.
        final ServiceLocator serviceLocator = this.serviceLocators.remove(environment);

        // disposed service locator.
        serviceLocator.dispose();
      }
    }
  }

  /**
   * Remove the service locator from the given host and port.
   *
   * @param factory the given factory information.
   * @param host the given host information.
   * @param port the given port information.
   */
  public void removeServiceLocator(final String factory, final String host, final String port) {
    // create the key.
    final String key = combineKey(factory, host, port);

    // remove the properties.
    synchronized (this.properties) {
      // remove the service locator.
      if (this.properties.containsKey(key)) {
        // get the properties.
        final Properties environment = this.properties.remove(key);

        // get service locator.
        final ServiceLocator serviceLocator = this.serviceLocators.remove(environment);

        // disposed service locator.
        serviceLocator.dispose();
      }
    }
  }

  /**
   * Remove the service locator from the given host and port.
   *
   * @param factory the given factory information.
   * @param pkgPrefix the PKG prefix information.
   * @param host the given host information.
   * @param port the given port information.
   */
  public void removeServiceLocator(final String factory, final String pkgPrefix,
      final String host, final String port) {
    // create the key.
    final String key = combineKey(factory, pkgPrefix, host, port);

    // remove the properties.
    synchronized (this.properties) {
      // remove the service locator.
      if (this.properties.containsKey(key)) {
        // get the properties.
        final Properties environment = this.properties.remove(key);

        // get service locator.
        final ServiceLocator serviceLocator = this.serviceLocators.remove(environment);

        // disposed service locator.
        serviceLocator.dispose();
      }
    }
  }

  /**
   * Dispose service locator manager.
   */
  public void dispose() {
    synchronized (this.serviceLocators) {
      for (final ServiceLocator serviceLocator : this.serviceLocators.values()) {
        if (serviceLocator != null) {
          serviceLocator.dispose();
        }
      }

      // clear all service locator.
      this.serviceLocators.clear();
      this.properties.clear();
    }
  }

  /**
   * Combine the key from the given list of parts.
   *
   * @param parts the given list of parts as String.
   * @return the combined key.
   */
  protected String combineKey(final String...parts) {
    // there is no part.
    if (parts == null || parts.length == 0) {
      return StringUtils.EMPTY_STRING;
    }

    // there is only one part.
    if (parts.length == 1) {
      return parts[0];
    }

    // has multiple parts.
    final StringBuilder keys = new StringBuilder();
    for (int index = 0; index < parts.length - 1; index++) {
      keys.append(parts[index].trim()).append(':');
    }

    // append the last part.
    keys.append(parts[parts.length - 1].trim());

    return keys.toString();
  }
}
