package id.co.sambaltomat.core.dao;

import id.co.sambaltomat.core.dao.hibernate.GenericDaoHibernate;
import id.co.sambaltomat.core.model.SearchCriteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Generic DAO (Data Access Object) with common methods to CRUD POJOs.
 * <p/>
 * <p>Extend this interface if you want typesafe (no casting necessary) DAO's for your
 * domain objects.
 *
 * @author <a href="mailto:bwnoll@gmail.com">Bryan Noll</a>
 * @param <T> a type variable
 * @param <PK> the primary key for that type
 */
public interface GenericDao<T, PK extends Serializable> {

    enum JoinType
    {
        INNER_JOIN,
        LEFT_JOIN,

        //menggunakan mode fetch (proxy) data akan diinitialize (ada extra retrieve)
        FETCH_MODE_SELECT,

        //menggunakan mode fetch (proxy) tapi data tidak diinitialize (masih berupa proxy)
        FETCH_MODE_SELECT_UNINITIALIZE,

        //dipakai untuk memfilter record hanya mengambil sesuai record parent modelnya
        //kasus=query left join many to one (jumlah record parent akan = jumlah record childnya)
        NON_DISTINCT_ROOT_ENTITY,

        //menambahkan statement group pada query, biasanya digunakan
        //secara simultan dengan NON_DISTINCT_ROOT_ENTITY
        //yang dipakai sebagai acuan nama field adalah alias (JoinPath.alias)
        GROUPING_FIELD
    }

    /**
     * Dipakai untuk meninformasikan kepada Hibernate path ke property yang diinginkan dari model<br>
     * Misal relasi dengan bentuk :
     * <pre>
     *      modelA.relB.relC.relD
     * </pre>
     * harus dipecah menjadi beberapa alias agar dikenali hibernate
     * <pre>
     *      modelA.relB as aliasAB
     *      aliasAB.relC as aliasBC
     *      aliasBC.relD as aliasCD
     * </pre>
     * Dengan JoinPath di lakukan dengan
     * <pre>
     *      new JoinPath("modelA.relB","aliasAB",JoinType.INNER_JOIN)
     *      new JoinPath("aliasAB.relC","aliasBC",JoinType.INNER_JOIN)
     *      new JoinPath("aliasBC.relD","aliasCD",JoinType.INNER_JOIN)
     * </pre>
     */
    public class JoinPath
    {
        public String path;
        public String alias;
        public JoinType joinType;

        public JoinPath(String path, String alias, JoinType joinType)
        {
            this.path = path;
            this.alias = alias;
            this.joinType = joinType;
        }
        public JoinPath(String path, String alias)
        {
            this.path = path;
            this.alias = alias;
            //default join type adalah inner join (seperti default HQL)
            this.joinType = JoinType.INNER_JOIN;
        }
    }

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
     * @param id the id of the entity
     * @return - true if it exists, false if it doesn't
     */
    boolean exists(PK id);

    /**
     * Generic method to save an object - handles both update and insert.
     *
     * @param object the object to save
     * @return the persisted object
     */
    T save(T object);

    /**
     * Try to use insert instead of update.
     *
     * @param object the object to save
     * @return the persisted object
     */
    T forceInsert(T object);

    /**
     * Generic method to delete an object based on class and id
     *
     * @param id the identifier (primary key) of the object to remove
     */
    void remove(PK id);

    /**
     * Gets all records without duplicates.
     * <p>Note that if you use this method, it is imperative that your model
     * classes correctly implement the hashcode/equals methods</p>
     *
     * @return List of populated objects
     */
    List<T> getAllDistinct();


    /**
     * Find a list of records by using a named query
     *
     * @param queryName   query name of the named query
     * @param queryParams a map of the query names and the values
     * @return a list of the records found
     */
    List<T> findByNamedQuery(String queryName, Map<String, Object> queryParams);

    /**
     * Get a number of records which size is limited pageSize starting from the
     * index of pageNumber.
     *
     * @param first
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @param params
     * @return
     */
    List<T> getCurrentPageRows(int first, int pageSize, String sortColumn, boolean sortOrder, Map<String, String> params);

    int getRowCount(List<Criterion> params);
    int getRowCount(List<Criterion> params, List<GenericDaoHibernate.JoinPath> joinPaths);

    List<T> getSinglePage(int firstRow, int pageSize, List<Criterion> params, Order[] orders);
    List<T> getSinglePage(int firstRow, int pageSize, List<GenericDaoHibernate.JoinPath> joinPaths, List<Criterion> params, Order[] orders);

    public List<T> getList(final List<JoinPath> joinPaths, final List<Criterion> params, final Order[] orders);

    List<T> searchByPropertyCriteria(List<CriterionEntry> namedCriterionList, List<OrderEntry> orderList, int firstResult, int maxResults);

    List<T> searchByCriteria(SearchCriteria searchCriteria, int firstResult, int maxResults);

    Integer searchByCriteriaCount(SearchCriteria searchCriteria);

    public void clearCache();
}