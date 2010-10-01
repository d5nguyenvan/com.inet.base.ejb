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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.ejb3.entity.HibernateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inet.base.ejb.conf.ConfigBean;
import com.inet.base.ejb.exception.EjbException;
import com.inet.base.ejb.internal.StringUtils;

/**
 * BaseManageSessionBean.
 * @param <K> the given primary key data type.
 * @param <T> the given persistence data type.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BaseManageSessionBean.java 2007-12-31 10:44:52z nguyen_dv $
 * @since 1.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
public abstract class BaseManageSessionBean<K, T> {
  //~ static fields =========================================================
  /**
   * class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BaseManageSessionBean.class);

  /**
   * remote reference.
   */
  protected static final String REMOTE = "/remote";

  /**
   * local reference.
   */
  protected static final String LOCAL = "/local";

  //~ instance fields =======================================================
  /**
   * Entity bean manager.
   */
  protected EntityManager entityManager;

  /**
   * User transaction.
   */
  protected UserTransaction userTransaction;

  /**
   * session context.
   */
  protected SessionContext sessionContext;

  /**
   * Set {@link EntityManager entity manager} instance.
   *
   * @param em the given {@link EntityManager entity manager} to set.
   */
  @PersistenceContext
  public void setEntityManager(final EntityManager em) {
    this.entityManager = em;
  }

  /**
   * Returns the current {@link EntityManager entity manager} instance.
   *
   * @return the current {@link EntityManager entity manager} instance.
   */
  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Returns the current {@link UserTransaction user transaction} instance.
   *
   * @return the current {@link UserTransaction user transaction} instance.
   */
  public UserTransaction getUserTransaction() {
    return userTransaction;
  }

  /**
   * Set {@link UserTransaction user transaction} to manage session bean.
   *
   * @param transaction the given {@link UserTransaction user transaction} to set.
   */
  @Resource
  public void setUserTransaction(final UserTransaction transaction) {
    this.userTransaction = transaction;
  }

  /**
   * Sets the {@link SessionContext session context} instance.
   *
   * @param context the given {@link SessionContext session context} to be set.
   */
  @Resource
  public void setSessionContext(final SessionContext context){
    this.sessionContext = context;
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
   * Build search query from the given persistence object.
   *
   * @param search the given persistence object.
   * @return the {@link Criteria criteria} instance.
   *
   * @throws EjbException if we could not build the criteria from the given persistence object.
   */
  protected abstract Criteria buildQuery(final T search) throws EjbException;

  /**
   * Creates the criteria from the given class.
   *
   * @param clazz the given class to create the {@link Criteria criteria} instance.
   * @return the {@link Criteria criteria} of given class.
   *
   * @throws EjbException if we could not create {@link Criteria criteria} from the given class.
   */
  protected Criteria getCriteria(final Class<?> clazz) throws EjbException {
    try {
      // get hibernate session.
      final HibernateSession hs = (HibernateSession) entityManager;

      // get the session.
      final Session session = hs.getHibernateSession();

      // create criteria from the session.
      return session.createCriteria(clazz);
    } catch (final Exception ex) {
      final String msg = "Could not create the criteria from class {" + clazz.getName() + "}";
      throw new EjbException(msg, ex);
    }
  }

  /**
   * Query and result the data from the given search persistence object.
   *
   * @param search the given persistence object used to build the criteria.
   * @return the list of data that match the given criteria.
   * @throws EjbException if we could not retrieves the list of persistence object from the
   * entity manager.
   */
  @SuppressWarnings({ "unchecked" })
  public List<T> query(final T search) throws EjbException {
    try {
      return buildQuery(search).list();
    } catch (final HibernateException hbex) {
      final String msg = "Could not retrieves the list of persistence objects.";
      throw new EjbException(msg, hbex);
    }
  }

  /**
   * Query and result the data from the given criteria.
   *
   * @param criteria the given {@link Criteria criteria} to be query data.
   * @return the list of data that match the given criteria.
   *
   * @throws EjbException if we could not execute the query to retrieve the persistence object
   * from the entity manager.
   */
  @SuppressWarnings({ "unchecked" })
  public List<T> query(final Criteria criteria) throws EjbException{
    if (criteria == null) {
      return null;
    }

    try {
      // return data.
      return (List<T>) criteria.list();
    } catch (final HibernateException hbex) {
      final String msg = "Could not execute query to retrieve the list of persistence object.";
      throw new EjbException(msg, hbex);
    }
  }

  /**
   * Query and result the data from the given search persistence object.
   *
   * @param search the given persistence object used to build the criteria.
   * @param startAt the start at item the user want to retrieve.
   * @param maxItems the max items the user want to retrieve.
   *
   * @return the list of data that match the given criteria.
   *
   * @throws EjbException if we could not build and execute query to retrieve objects from the
   * container.
   */
  @SuppressWarnings({ "unchecked" })
  public List<T> query(final T search, final int startAt, final int maxItems) throws EjbException {
    try {
      // create criteria from the given persistence object.
      final Criteria criteria = buildQuery(search);

      // set the limit objects that user want to retrieve.
      criteria.setFirstResult(startAt);
      criteria.setMaxResults(maxItems);

      return criteria.list();
    } catch (final HibernateException hbex) {
      final String msg = "Could not execute query to retrieve the objects from the container.";
      throw new EjbException(msg, hbex);
    }
  }

  /**
   * Persist persistence the transient object to container.
   *
   * @param obj the given transient object to persist.
   * @throws EjbException if could not persist the transient object or the given object is not
   * a transient object.
   */
  protected T insert(final T obj) throws EjbException {
    try {
      entityManager.persist(obj);

      // return object.
      return obj;
    } catch (final EntityExistsException eeex) {
      throw new EjbException(eeex);
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Merger persistence object.
   *
   * @param obj the given persistence object to merged.
   * @throws EjbException if the given object is not persistence object or we could not merge the
   * persistence object to container.
   */
  protected T update(final T obj) throws EjbException {
    try {
      entityManager.merge(obj);

      // return object.
      return obj;
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Remove persistence object.
   *
   * @param obj the given persistence object to remove.
   * @throws EjbException if the given object is not persistence object or we could not remove
   * object out of container.
   */
  protected void remove(final T obj) throws EjbException {
    try {
      entityManager.remove(obj);
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Remove persistence object by primary key.
   *
   * @param key the given primary key of the object to be removed.
   * @param clazz the given persistence object type.
   *
   * @throws EjbException if we could not remove the object out of the container.
   */
  protected void remove(final K key, final Class<T> clazz) throws EjbException {
    try {
      final T obj = (T) entityManager.find(clazz, key);
      if (obj != null) {
        entityManager.remove(obj);
      }
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load persistence object by primary key.
   *
   * @param key the given primary key of persistence object
   * @param clazz the given persistence object type.
   *
   * @return the persistence object instance or null.
   *
   * @throws EjbException if we could not load the persistence object from the container.
   */
  protected T load(final K key, final Class<T> clazz) throws EjbException {
    try {
      return (T) entityManager.find(clazz, key);
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load single persistence object.
   *
   * @param query the given query to load persistence object.
   *
   * @return persistence object or null.
   *
   * @throws EjbException if we could not execute the query or there are many object
   * the matched with the criteria in the query.
   */
  @SuppressWarnings({ "unchecked" })
  protected T loadSingle(final Query query) throws EjbException {
    try {
      return (T) query.getSingleResult();
    } catch (final NoResultException nrex) {
      return null;
    } catch (final NonUniqueResultException nurex) {
      throw new EjbException(nurex);
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load all persistence objects that match the given criteria in query.
   *
   * @param query the given {@link Query query} to load persistence object.
   * @return the list of persistence object to match with criteria in query.
   *
   * @throws EjbException if we could not execute the query.
   */
  @SuppressWarnings({ "unchecked" })
  protected List<T> load(final Query query) throws EjbException {
    try {
      return (List<T>) query.getResultList();
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load the first persistence object that match the criteria given by query.
   *
   * @param namedQuery the given name query to be load to execute.
   * @param param the given query parameter name.
   * @param key the given primary key value to set.
   *
   * @return the first persistence object or null.
   *
   * @throws EjbException if we could not execute the query or there are many object
   * that match the criteria.
   */
  @SuppressWarnings({ "unchecked" })
  protected T load(final String namedQuery, final String param, final K key) throws EjbException {
    try {
      // create query.
      final Query query = getEntityManager().createNamedQuery(namedQuery);
      query.setParameter(param, key);
      query.setMaxResults(1);

      // execute and return the value.
      final List results = query.getResultList();

      return (T) (results != null && results.size() > 0 ? results.get(0) : null);
    } catch (final NoResultException nrex) {
      return null;
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load all persistence objects that match the criteria given by query.
   *
   * @param namedQuery the given name query to be load to execute.
   * @param params the given list of pairs (parameter, value).
   * @return all persistence objects; never {@code null}.
   *
   * @throws EjbException if we could not execute the query.
   */
  @SuppressWarnings({ "unchecked" })
  protected <V> List<V> load(final String namedQuery, final Map<String, Object> params)
    throws EjbException {
    try {
      // create query.
      final Query query = getEntityManager().createNamedQuery(namedQuery);

      if (params != null && !params.isEmpty()) {
        for (final String param : params.keySet()) {
          final Object value = params.get(param);
          if (value != null) {
            query.setParameter(param, value);
          }
        }
      }

      // execute and return the value.
      final List result = query.getResultList();

      // return result.
      return (List<V>) (result == null ? Collections.emptyList() : result);
    } catch (final NoResultException nrex) {
      return Collections.emptyList();
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Load list of persistence objects dependence on start position and page
   * items.
   *
   * @param query the given {@link Query query} instance to query the objects.
   * @param startAt the given start position to get result.
   * @param maxItems the given maximize items to retrieve.
   *
   * @return the list of persistence objects to fetch.
   * @throws EjbException if we could not execute the query.
   */
  @SuppressWarnings({ "unchecked" })
  protected List<T> pagination(final Query query, final int startAt, final int maxItems)
    throws EjbException {
    try {
      query.setFirstResult(startAt);
      query.setMaxResults(maxItems);

      return (List<T>) query.getResultList();
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Synchronized current persistence context to underlying database.
   *
   * @throws EjbException if we could not synch the status to database.
   */
  protected void flush() throws EjbException {
    try {
      entityManager.flush();
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final PersistenceException pex) {
      throw new EjbException(pex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Set flush mode.
   *
   * @param modeType the given {@link FlushModeType flush mode} to set.
   */
  protected void setFlushMode(final FlushModeType modeType) {
    entityManager.setFlushMode(modeType);
  }

  /**
   * Merge persistence object.
   *
   * @param obj the given persistence object to merge.
   *
   * @return the persistence object after merge.
   *
   * @throws EjbException if the given object is not persistence object or we could not
   * merge the object to container.
   */
  protected T merge(final T obj) throws EjbException {
    try {
      final T result = entityManager.merge(obj);
      return result;
    } catch (final EntityExistsException eeex) {
      throw new EjbException(eeex);
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      throw new EjbException(throwable);
    }
  }

  /**
   * Execute the query and return the number of rows is changed.
   *
   * @param query the query to execute.
   * @return the number of rows is changed.
   * @throws EjbException if an error occurs during executing update query.
   */
  protected int executeUpdate(final Query query) throws EjbException {
    try {
      return query.executeUpdate();
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final EJBException eex) {
      throw new EjbException(eex);
    } catch (final Throwable th) {
      throw new EjbException(th);
    }
  }

  /**
   * Execute the query and return the number of rows is changed.
   *
   * @param namedQuery the query to execute.
   * @param params the map of query parameters.
   * @return the number of rows is changed.
   * @throws EjbException if an error occurs during executing update query.
   */
  protected int executeUpdate(final String namedQuery, final Map<String, Object> params)
    throws EjbException {
    try {
      // create query.
      final Query query = getEntityManager().createNamedQuery(namedQuery);

      if (params != null && !params.isEmpty()) {
        for (final String param : params.keySet()) {
          final Object value = params.get(param);
          if (value != null) {
            query.setParameter(param, value);
          }
        }
      }

      return query.executeUpdate();
    } catch (final IllegalArgumentException iaex) {
      throw new EjbException(iaex);
    } catch (final IllegalStateException isex) {
      throw new EjbException(isex);
    } catch (final EJBException eex) {
      throw new EjbException(eex);
    } catch (final Throwable th) {
      throw new EjbException(th);
    }
  }

  /**
   * Looks up the remote bean from the bean name and the type of bean.
   * @param <V> the given bean type.
   *
   * @param beanName the given bean name to lookup.
   * @param clazz the bean type.
   *
   * @return the remote bean instance.
   */
  protected <V> V lookupRemote(final String beanName, final Class<V> clazz) {
    if (beanName != null) {
      try {
        // lookup remote bean object.
        final Object remoteObject = sessionContext.lookup(getFullBeanPath(beanName + REMOTE));

        // cast and return this bean.
        return clazz.cast(remoteObject);
      } catch (final Exception ex) {
        LOG.warn("Could not lookup the remote bean {}", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Looks up the local bean from the given bean name and the type of bean.
   * @param <V> the given local bean type.
   *
   * @param beanName the given local bean name.
   * @param clazz the given local bean type.
   *
   * @return the local bean instance.
   */
  protected <V> V lookupLocal(final String beanName, final Class<V> clazz) {
    if (beanName != null) {
      try {
        // lookup local bean.
        final Object localObject = sessionContext.lookup(getFullBeanPath(beanName + LOCAL));

        // cast and return this bean.
        return clazz.cast(localObject);
      } catch (final Exception ex) {
        LOG.warn("Could not lookup the local bean {}", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Count all items that match criteria.
   *
   * @param criteria the given {@link Criteria criteria} to be search.
   *
   * @return all items that match criteria.
   */
  public int count(final Criteria criteria) {
    try {
      // execute the criteria and get single result.
      final Object result = criteria.uniqueResult();

      // return all items that match criteria.
      if (result instanceof Integer) {
        return ((Integer) result).intValue();
      }

      if (result instanceof Long) {
        return ((Long) result).intValue();
      }
    } catch (final Exception ex) {
      LOG.warn("Could not execute the query or convert value.");
    }

    return 0;
  }

  /**
   * Count all items that match criteria.
   *
   * @param query the given {@link Query query} to be count.
   *
   * @return all items that match criteria.
   */
  public int count(final Query query) {
    try {
      // execute the criteria and get single result.
      final Object result = query.getSingleResult();

      // return all items that match criteria.
      if (result instanceof Long) {
        return ((Long) result).intValue();
      }

      if (result instanceof Integer) {
        return ((Integer) result).intValue();
      }
    } catch (final Exception ex) {
      LOG.warn("Could not execute the query or convert value.");
    }

    return 0;
  }

  /**
   * Roll-back transaction.
   */
  public void rollback() {
    try {
      userTransaction.rollback();
    } catch (final IllegalStateException ex) {
      LOG.warn("IllegalStateException {}", ex);
    } catch (final SecurityException sex) {
      LOG.warn("SecurityException {}", sex);
    } catch (final SystemException sex) {
      LOG.warn("SystemException {}", sex);
    }
  }

  /**
   * Begin transaction.
   */
  public void beginTransaction() {
    try {
      userTransaction.begin();
    } catch (final NotSupportedException nsex) {
      LOG.warn("NotSupportedException {}", nsex);
    } catch (final SystemException sex) {
      LOG.warn("SystemException {}", sex);
    }
  }

  /**
   * Commit transaction.
   */
  public void commit() {
    try {
      userTransaction.commit();
    } catch (final SecurityException sex) {
      LOG.warn("SecurityException {}", sex);
    } catch (final IllegalStateException isex) {
      LOG.warn("IllegalStateException {}", isex);
    } catch (final RollbackException rex) {
      LOG.warn("RollbackException {}", rex);
    } catch (final HeuristicMixedException hmex) {
      LOG.warn("HeuristicMixedException {}", hmex);
    } catch (final HeuristicRollbackException hrex) {
      LOG.warn("HeuristicRollbackException {}", hrex);
    } catch (final SystemException sex) {
      LOG.warn("SystemException {}", sex);
    }
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

  //~ helper functions ======================================================
  /**
   * Returns the current login user name.
   *
   * @return the current login user name; may be empty;
   */
  protected String getUsername() {
    try {
      // get caller principal name.
      String login = sessionContext.getCallerPrincipal().getName();
      if (LOG.isInfoEnabled()) {
        LOG.info("The login name: [{}]", login);
      }

      // check the login name and return the right value.
      return (!StringUtils.isset(login) ? StringUtils.EMPTY_STRING : login);
    } catch (final Exception ex) {
      LOG.warn("Could not get the current login name.");
    }

    // any ways return the blank value.
    return StringUtils.EMPTY_STRING;
  }
}
