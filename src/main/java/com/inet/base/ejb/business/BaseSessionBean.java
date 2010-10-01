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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.jboss.ejb3.entity.HibernateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inet.base.ejb.conf.ConfigBean;
import com.inet.base.ejb.exception.EjbException;
import com.inet.base.ejb.internal.StringUtils;

/**
 * BaseSessionBean.
 * @param <K> the primary key data type.
 * @param <T> the given persistence data type.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: BaseSessionBean.java 2009-07-08 nguyen_dv $
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public abstract class BaseSessionBean<K, T> {
  //~ static fields =========================================================
  /**
   * class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BaseSessionBean.class);

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
   * entity manager.
   */
  protected EntityManager entityManager;

  /**
   * session context.
   */
  protected SessionContext sessionContext;

  /**
   * Set the {@link EntityManager entity manager} instance.
   *
   * @param em the given {@link EntityManager entity manager} instance to set.
   */
  @PersistenceContext
  public void setEntityManager(EntityManager em) {
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
   * Sets the {@link SessionContext session context} to session bean manager.
   *
   * @param context the given {@link SessionContext session context} to be set.
   */
  @Resource
  public void setSessionContext(final SessionContext context) {
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
   * @param search the given persistence object to build search criteria.
   *
   * @return the search {@link Criteria criteria} instance.
   *
   * @throws EjbException if we could not build search {@link Criteria criteria} from the
   * persistence object.
   */
  protected abstract Criteria buildQuery(final T search) throws EjbException;

  /**
   * Creates the criteria from the given class.
   *
   * @param clazz the given {@link Class class} to make {@link Criteria criteria} instance.
   * @return the {@link Criteria criteria} instance.
   *
   * @throws EjbException if web could not create {@link Criteria criteria} instance from
   * {@link Class class}.
   */
  protected Criteria getCriteria(final Class<?> clazz) throws EjbException {
    try {
      // get hibernate session.
      final HibernateSession hs = (HibernateSession) entityManager;

      // get the session.
      final Session session = hs.getHibernateSession();

      // create criteria from the session.
      return session.createCriteria(clazz);
    } catch (final HibernateException hex) {
      final String msg = "Could not create search criteria from class {" + clazz.getName() + "}";
      throw new EjbException(msg, hex);
    }
  }

  /**
   * Creates the criteria from the given class.
   *
   * @param clazz the given {@link Class class} to create the {@link Criteria criteria}.
   * @param name the given criteria name.
   *
   * @return the {@link Criteria criteria} instance.
   *
   * @throws EjbException if we could not create {@link Criteria criteria} from {@link Class class}
   */
  protected Criteria getCriteria(Class<?> clazz, String name) throws EjbException {
    try {
      // get HibernateSession.
      final HibernateSession hs = (HibernateSession) entityManager;

      // get the session.
      final Session session = hs.getHibernateSession();

      // create criteria from the session.
      return session.createCriteria(clazz, name);
    } catch (final HibernateException hex) {
      final String msg = "Could not create search criteria from class {" + clazz.getName() + "}";
      throw new EjbException(msg, hex);
    }
  }

  /**
   * Query and return the data from the given search persistence object.
   *
   * @param search the given persistence object used to build the criteria.
   * @return the list of data that match the given criteria.
   *
   * @throws EjbException if we could not build the search criteria or could not execute query
   * to retrieve data from container.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<T> query(final T search) throws EjbException {
    try {
      return buildQuery(search).list();
    } catch (final HibernateException hex) {
      final String msg = "Could not execute the query to retrieve the data from container.";
      throw new EjbException(msg, hex);
    }
  }

  /**
   * Query and result the data from the given criteria.
   *
   * @param criteria the given criteria to be query data.
   * @return the list of data that match the given criteria.
   *
   * @throws EjbException if we could not execute query to retrieve the matching data in the
   * container.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<T> query(final Criteria criteria) throws EjbException {
    try {
      if (criteria == null) {
        return null;
      }

      // return data.
      return (List<T>) criteria.list();
    } catch (final HibernateException hex) {
      final String msg = "Could not execute query to retrieve the matching the data.";
      throw new EjbException(msg, hex);
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
   * @throws EjbException if we could not execute query to retrieve the matching data in container.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<T> query(final T search, final int startAt, final int maxItems) throws EjbException {
    try {
      // create criteria from the given persistence object.
      final Criteria criteria = buildQuery(search);

      // set the limit objects that user want to retrieve.
      criteria.setFirstResult(startAt);
      criteria.setMaxResults(maxItems);

      return criteria.list();
    } catch (final HibernateException hex) {
      final String msg = "Could not execute query to retrieve the matching data in container.";
      throw new EjbException(msg, hex);
    }
  }

  /**
   * Persist the transient object to container.
   *
   * @param obj the given transient object to persit.
   *
   * @return the persistence object.
   *
   * @throws EjbException if we could not persit transient object to container.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected T insert(final T obj) throws EjbException {
    try {
      entityManager.persist(obj);

      // return object.
      return obj;
    } catch (final EntityExistsException eeex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(eeex);
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Merger the persistence object.
   *
   * @param obj the given persistence object to merge.
   *
   * @return the persistence object.
   * @throws EjbException if we could not merge persistence object.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected T update(final T obj) throws EjbException {
    try {
      entityManager.merge(obj);

      // return merge object.
      return obj;
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Remove persistence object.
   *
   * @param obj the given persistence object to remove.
   *
   * @throws EjbException if the given object is not persistence or could not remove persistence
   * out of the container.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected void remove(final T obj) throws EjbException {
    try {
      entityManager.remove(obj);
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Remove persistence object by primary key.
   *
   * @param key the given primary key to find the persistence object to remove.
   * @param clazz the given persistence object type.
   *
   * @throws EjbException if we could not remove the persistence object from the given primary
   * key out of the container.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected void remove(final K key, final Class<T> clazz) throws EjbException {
    try {
      final T obj = (T) entityManager.find(clazz, key);
      if (obj != null) {
        entityManager.remove(obj);
      }
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Load persistence object by primary key.
   *
   * @param key the given primary key to load persistence object.
   * @param clazz the given persistence object type.
   *
   * @return the given persistence object instance.
   * @throws EjbException if we could not load the persistence object from the given
   * primary key.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * @param query the given {@link Query query} to retrieve the data.
   * @return the persistence object or null.
   *
   * @throws EjbException if we could not execute query to retrieve data or there are many
   * persistence object that matching the search criteria.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * Load all persistence object that match criteria in query.
   *
   * @param query the given {@link Query query} to search data.
   * @return the list of persistence objects.
   *
   * @throws EjbException if we could not search data in container.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * Load persistence object from the given name query and persistence primary key.
   *
   * @param namedQuery the given name query to build the query.
   * @param param the given query parameter.
   * @param key the given primary key value.
   *
   * @return the persistence object that matching with criteria.
   *
   * @throws EjbException if we could not load data from container or there are many object that
   * matching with the criteria.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * @param query the given {@link Query query} to query data.
   * @param startAt the given start position to retrieve the data.
   * @param maxItems the given max elements to fetch.
   *
   * @return the list of matching objects.
   * @throws EjbException if we could not execute query to fetch matching object.
   */
  @SuppressWarnings({ "unchecked" })
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * Execute the query and return the number of rows is changed.
   *
   * @param query the query to execute.
   * @return the number of rows is changed.
   * @throws EjbException if an error occurs during executing update query.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected int executeUpdate(final Query query) throws EjbException {
    try {
      return query.executeUpdate();
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final IllegalStateException isex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(isex);
    } catch (final EJBException eex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(eex);
    } catch (final Throwable th) {
      sessionContext.setRollbackOnly();
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
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
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
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final IllegalStateException isex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(isex);
    } catch (final EJBException eex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(eex);
    } catch (final Throwable th) {
      sessionContext.setRollbackOnly();
      throw new EjbException(th);
    }
  }

  /**
   * Synchronized current persistence context to underlying database.
   *
   * @throws EjbException if we could not synch the container data and database data.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected void flush() throws EjbException {
    try {
      entityManager.flush();
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final PersistenceException pex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(pex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Set flush mode.
   *
   * @param modeType the given {@link FlushModeType flush mode} to set.
   */
  protected void setFlushMode(final FlushModeType modeType) {
    this.entityManager.setFlushMode(modeType);
  }

  /**
   * Merge persistence object.
   *
   * @param obj the given persistence object to merge.
   *
   * @return the persistence object.
   *
   * @throws EjbException if we could not merges the persistence object or the given object
   * is not persistence object.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  protected T merge(final T obj) throws EjbException {
    try {
      return entityManager.merge(obj);
    } catch (final EntityExistsException eeex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(eeex);
    } catch (final IllegalArgumentException iaex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(iaex);
    } catch (final TransactionRequiredException trex) {
      sessionContext.setRollbackOnly();
      throw new EjbException(trex);
    } catch (final Throwable throwable) {
      sessionContext.setRollbackOnly();
      throw new EjbException(throwable);
    }
  }

  /**
   * Looks up the remote bean from the bean name and the type of bean.
   * @param <V> the given remote bean type.
   *
   * @param beanName the given remote bean name to lookup.
   * @param clazz the given remote bean type.
   *
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
        LOG.warn("Could not lookup the remote bean {}.", beanName);
      }
    }

    // return null if error occur.
    return null;
  }

  /**
   * Looks up the local bean from the given bean name and the type of bean.
   * @param <V> the local bean type.
   *
   * @param beanName the given local bean name to lookup.
   * @param clazz the given bean type.
   *
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
        LOG.warn("Could not lookup the local bean {}.", beanName);
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
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
   * @return all items that match criteria.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
      LOG.warn("Could not execute the query or convert value", ex);
    }

    return 0;
  }

  /**
   * Calculate the value of criteria.
   *
   * @param criteria the given {@link Criteria criteria} to be calculate.
   * @return the number of calculate query
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public long calculate(final Criteria criteria) {
    try {
      // execute the criteria and get single result.
      final Object result = criteria.uniqueResult();

      // return all items that match criteria.
      if (result instanceof Long) {
        return ((Long) result).longValue();
      }

      // return all items that match criteria.
      if (result instanceof Integer) {
        return ((Integer) result).longValue();
      }

    } catch (final Exception ex) {
      LOG.warn("Could not execute the query or convert value.");
    }

    return 0;
  }

  /**
   * Calculate the value of query.
   *
   * @param query the given {@link Query query} to be calculate.
   * @return the number of calculate query
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public long calculate(final Query query) {
    try {
      // execute the criteria and get single result.
      Object result = query.getSingleResult();

      // return all items that match criteria.
      if (result instanceof Long) {
        return ((Long) result).longValue();
      }

      if (result instanceof Integer) {
        return ((Integer) result).longValue();
      }
    } catch (final Exception ex) {
      LOG.warn("Could not execute the query or convert value", ex);
    }

    return 0;
  }

  //~ helper functions ======================================================
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
   * Returns the current login user name.
   *
   * @return current login user name; may be empty.
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
      LOG.warn("Could not get the current login name.");
    }

    // any ways return the blank value.
    return StringUtils.EMPTY_STRING;
  }
}
