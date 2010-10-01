/*****************************************************************
   Copyright 2009 by Tung Luong (lqtung@truthinet.com.vn)

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
package com.inet.base.ejb.conf;

/**
 * ConfigBean.
 * 
 * @author <a href="mailto:lqtung@truthinet.com.vn">Tung Luong</a>
 * @version 3.3i
 * 
 * YOUR APPLICATION MUST LOAD THIS CLASS IN CONTAINER OTHERWISE THE INSTALL IS NULL The
 * configuration file load this class in app.ear/config.sar/META-INF/config-jboss-beans.xml
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <deployment xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xsi:schemaLocation="urn:jboss:bean-deployer:2.0 bean-deployer_2_0.xsd"
 *   xmlns="urn:jboss:bean-deployer:2.0">
 *   <bean name="ConfigBean" class="com.inet.base.ejb.conf.ConfigBean">
 *     <property name="appLookup">webos/</property>
 *   </bean>
 * </deployment>
 */
public class ConfigBean {
  /** instance ConfigBean. */
  private static ConfigBean instance;

  /** The given application lookup name prefix. */
  private String appLookup;

  /**
   * Constructor which JBOSS MICROCONTAINER initialize this bean
   */
  public ConfigBean() {
    ConfigBean.instance = this;
  }

  /**
   * @return the instance
   */
  public static ConfigBean getInstance() {
    return instance;
  }

  /**
   * @return the appLookup
   */
  public String getAppLookup() {
    return this.appLookup;
  }

  /**
   * @param appLookup the appLookup to set
   */
  public void setAppLookup(String appLookup) {
    this.appLookup = appLookup;
  }
}
