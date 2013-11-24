package id.co.sambaltomat.core.service;

import id.co.sambaltomat.core.dao.CriterionEntry;
import id.co.sambaltomat.core.dao.OrderEntry;
import id.co.sambaltomat.core.dao.hibernate.GenericDaoHibernate;
import id.co.sambaltomat.core.model.SearchCriteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Generic Manager that talks to GenericDao to CRUD POJOs.
 * <p/>
 * <p>Extend this interface if you want typesafe (no casting necessary) managers
 * for your domain objects.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @param <T> a type variable
 * @param <PK> the primary key for that type
 */
public interface GenericManager<T, PK extends Serializable> {

    /**
     * Generic method used to get all objects of a particular type. This
     * is the same as lookup up all rows in a table.
     *
     * @return List of populated objects
     */
    List<T> getAll();

    /**
     * Generic method to get an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if
     * nothing is found.
     *
     * @param id the identifier (primary key) of the object to get
     * @return a populated object
     * @see org.springframework.orm.ObjectRetrievalFailureException
     */
    T get(PK id);

    /**
     * Select specific column names
     *
     * @param id primary key
     * @param columnNames argumen dinamis, sebutkan nama2 column string yang di pilih dalam SELECT
     * @return
     */
    public T get(final PK id, final String... columnNames);

    /**
     * Checks for existence of an object of type T using the id arg.
     *
     * @param id the identifier (primary key) of the object to get
     * @return - true if it exists, false if it doesn't
     */
    boolean exists(PK id);

    /**
     * Generic method to save an object - handles both update and insert.
     *
     * @param object the object to save
     * @return the updated object
     */
    T save(T object);

    /**
     * Force to use Insert instead of update.
     *
     * @param object the object to save
     * @return the updated object
     */
    T forceInsert(T object);

    /**
     * Generic method to delete an object based on class and id
     *
     * @param id the identifier (primary key) of the object to remove
     */
    void remove(PK id);

    /**
     *
     * @param first
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @param params
     * @return
     */
    List<T> getCurrentPageRows(int first, int pageSize, String sortColumn, boolean sortOrder, Map<String, String> params);

    /**
     *
     * @param params
     * @return
     */
    int getRowCount(List<Criterion> params);

    /**
     * Alternative row count, instead of extending Generic* class, try to map the entity alias in Join Path 
     * @param params
     * @param joinPaths Join Path is alias list to allow to reach deep entity
     * @return
     */
    int getRowCount(List<Criterion> params, List<GenericDaoHibernate.JoinPath> joinPaths);

    /**
     *
     * @param firstRow
     * @param pageSize
     * @param params
     * @param orders
     * @return
     */
    List<T> getSinglePage(int firstRow, int pageSize, List<Criterion> params, Order[] orders);

    /**
     * Alternative getSinglePage, instead of extending Generic* class, try to map the entity alias in Join Path
     * @param firstRow
     * @param pageSize
     * @param joinPaths
     * @param params
     * @param orders
     * @return
     */
    List<T> getSinglePage(int firstRow, int pageSize, List<GenericDaoHibernate.JoinPath> joinPaths, List<Criterion> params, Order[] orders);

    List<T> getByPropertyCriteria(List<CriterionEntry> namedCriterionList, List<OrderEntry> orderList, int firstResult, int maxResults);

    List<T> searchByCriteria(SearchCriteria searchCriteria, int firstResult, int maxResults);

    Integer searchByCriteriaCount(SearchCriteria searchCriteria);

    void clearCache();
}
