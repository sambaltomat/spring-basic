package id.co.sambaltomat.core.service.impl;

import id.co.sambaltomat.core.dao.CriterionEntry;
import id.co.sambaltomat.core.dao.GenericDao;
import id.co.sambaltomat.core.dao.OrderEntry;
import id.co.sambaltomat.core.dao.hibernate.GenericDaoHibernate;
import id.co.sambaltomat.core.model.SearchCriteria;
import id.co.sambaltomat.core.service.GenericManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class serves as the Base class for all other Managers - namely to hold
 * common CRUD methods that they might all use. You should only need to extend
 * this class when your require custom CRUD logic.
 * <p/>
 * <p>To register this class in your Spring context file, use the following XML.
 * <pre>
 *     &lt;bean id="userManager" class="id.co.sambaltomat.core.service.impl.GenericManagerImpl"&gt;
 *         &lt;constructor-arg&gt;
 *             &lt;bean class="id.co.sambaltomat.core.dao.hibernate.GenericDaoHibernate"&gt;
 *                 &lt;constructor-arg value="id.co.sambaltomat.security.model.User"/&gt;
 *                 &lt;property name="sessionFactory" ref="sessionFactory"/&gt;
 *             &lt;/bean&gt;
 *         &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * </pre>
 * <p/>
 * <p>If you're using iBATIS instead of Hibernate, use:
 * <pre>
 *     &lt;bean id="userManager" class="id.co.sambaltomat.core.service.impl.GenericManagerImpl"&gt;
 *         &lt;constructor-arg&gt;
 *             &lt;bean class="id.co.sambaltomat.core.dao.ibatis.GenericDaoiBatis"&gt;
 *                 &lt;constructor-arg value="id.co.sambaltomat.security.model.User"/&gt;
 *                 &lt;property name="dataSource" ref="dataSource"/&gt;
 *                 &lt;property name="sqlMapClient" ref="sqlMapClient"/&gt;
 *             &lt;/bean&gt;
 *         &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * </pre>
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @param <T> a type variable
 * @param <PK> the primary key for that type
 */
public class GenericManagerImpl<T, PK extends Serializable> implements GenericManager<T, PK> {
    /**
     * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
     */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * GenericDao instance, set by constructor of this class
     */
    protected GenericDao<T, PK> genericDao;

    /**
     * Public constructor for creating a new GenericManagerImpl.
     *
     * @param genericDao the GenericDao to use for persistence
     */
    public GenericManagerImpl(final GenericDao<T, PK> genericDao) {
        this.genericDao = genericDao;
    }

    /**
     * {@inheritDoc}
     */
    public List<T> getAll() {
        return genericDao.getAll();
    }

    /**
     * {@inheritDoc}
     */
    public T get(PK id) {
        return genericDao.get(id);
    }

    /**
     * Select specific column names
     *
     * @param id primary key
     * @param columnNames argumen dinamis, sebutkan nama2 column string yang di pilih dalam SELECT
     * @return
     */
    public T get(final PK id, final String... columnNames)
    {
        return genericDao.get(id, columnNames);
    }
    /**
     * {@inheritDoc}
     */
    public boolean exists(PK id) {
        return genericDao.exists(id);
    }

    /**
     * {@inheritDoc}
     */
    public T save(T object) {
        return genericDao.save(object);
    }

    /**
     * {@inheritDoc}
     */
    public T forceInsert(T object) {
        return genericDao.forceInsert(object);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(PK id) {
        genericDao.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    public List<T> getCurrentPageRows(int first, int pageSize, String sortColumn, boolean sortOrder, Map<String, String> params) {
        return genericDao.getCurrentPageRows(first, pageSize, sortColumn, sortOrder, params);
    }

    public int getRowCount(List<Criterion> params) {
        return genericDao.getRowCount(params);
    }

    public int getRowCount(List<Criterion> params, List<GenericDaoHibernate.JoinPath> joinPaths)
    {
        return genericDao.getRowCount(params, joinPaths);
    }

    public List<T> getSinglePage(int firstRow, int pageSize, List<Criterion> params, Order[] orders) {
        return genericDao.getSinglePage(firstRow, pageSize, params, orders);
    }
    public List<T> getSinglePage(int firstRow, int pageSize, List<GenericDaoHibernate.JoinPath> joinPaths, List<Criterion> params, Order[] orders)
    {
        return genericDao.getSinglePage(firstRow, pageSize, joinPaths, params, orders);
    }

    public List<T> getByPropertyCriteria(List<CriterionEntry> namedCriterionList,  List<OrderEntry> orderList, int firstResult, int maxResults){
        return genericDao.searchByPropertyCriteria(namedCriterionList,orderList,firstResult,maxResults);
    }

    public List<T> searchByCriteria(SearchCriteria searchCriteria, int firstResult, int maxResults){
        return genericDao.searchByCriteria(searchCriteria, firstResult, maxResults);
    }

    /**
     * {@inheritDoc}
     */
	public Integer searchByCriteriaCount(SearchCriteria searchCriteria){
		return genericDao.searchByCriteriaCount(searchCriteria);
	}

    public void clearCache(){
        genericDao.clearCache();
    }
        

}
