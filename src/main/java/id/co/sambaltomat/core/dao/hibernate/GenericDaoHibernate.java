package id.co.sambaltomat.core.dao.hibernate;

import id.co.sambaltomat.core.dao.CriterionEntry;
import id.co.sambaltomat.core.dao.GenericDao;
import id.co.sambaltomat.core.dao.OrderEntry;
import id.co.sambaltomat.core.model.SearchCriteria;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * This class serves as the Base class for all other DAOs - namely to hold
 * common CRUD methods that they might all use. You should only need to extend
 * this class when your require custom CRUD logic.
 * <p/>
 * <p>To register this class in your Spring context file, use the following XML.
 * <pre>
 *      &lt;bean id="fooDao" class="id.co.sambaltomat.core.dao.hibernate.GenericDaoHibernate"&gt;
 *          &lt;constructor-arg value="id.co.sambaltomat.core.model.Foo"/&gt;
 *          &lt;property name="sessionFactory" ref="sessionFactory"/&gt;
 *      &lt;/bean&gt;
 * </pre>
 *
 * @author <a href="mailto:bwnoll@gmail.com">Bryan Noll</a>
 * @param <T> a type variable
 * @param <PK> the primary key for that type
 */
public class GenericDaoHibernate<T, PK extends Serializable> extends HibernateDaoSupport implements GenericDao<T, PK> {
    /**
     * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
     */
    protected final Log log = LogFactory.getLog(getClass());
    protected Class<T> persistentClass;


    /**
     * Constructor that takes in a class to see which type of entity to persist
     *
     * @param persistentClass the class type you'd like to persist
     */
    public GenericDaoHibernate(final Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }



    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll() {
//
//        Query q = getSessionFactory().getCurrentSession().createQuery("from "+this.persistentClass.getName());
//        q.setCacheable(true);
//
//        return q.list();

        return super.getHibernateTemplate().loadAll(this.persistentClass);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<T> getAllDistinct() {
        Collection result = new LinkedHashSet(getAll());
        return new ArrayList(result);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T get(PK id) {
        T entity = (T) super.getHibernateTemplate().get(this.persistentClass, id);

        if (entity == null) {
            ObjectRetrievalFailureException objectRetrievalFailureException = new ObjectRetrievalFailureException(this.persistentClass, id);
            log.warn("Uh oh, '" + this.persistentClass + "' object with id '" + id + "' not found...", objectRetrievalFailureException);
            throw objectRetrievalFailureException;
        }

        return entity;
    }

    /**
     * Select specific column names
     *
     * @param id primary key
     * @param columnNames argumen dinamis, sebutkan nama2 column string yang di pilih dalam SELECT
     * @return
     */
    public T get(final PK id, final String...columnNames)
    {
        return (T) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Criteria criteria = session.createCriteria(persistentClass,"talias");
                criteria.add(Restrictions.idEq(id));
                ProjectionList projectionList = Projections.projectionList();
                for (String columnName : columnNames)
                {
                    projectionList.add(Projections.property("talias." + columnName),columnName);
                }
                criteria.setProjection(projectionList);
                criteria.setResultTransformer(new AliasToBeanResultTransformer(persistentClass));
                Object returnObject = criteria.uniqueResult();
                return returnObject;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean exists(PK id) {
        T entity = (T) super.getHibernateTemplate().get(this.persistentClass, id);
        return entity != null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T save(T object) {
        return (T) super.getHibernateTemplate().merge(object);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T forceInsert(final T object) {
        return (T) super.getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                return session.save(object);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void remove(PK id) {
        super.getHibernateTemplate().delete(this.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<T> findByNamedQuery(
            String queryName,
            Map<String, Object> queryParams) {
        String[] params = new String[queryParams.size()];
        Object[] values = new Object[queryParams.size()];
        int index = 0;
        Iterator<String> i = queryParams.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            params[index] = key;
            values[index++] = queryParams.get(key);
        }
        return getHibernateTemplate().findByNamedQueryAndNamedParam(
                queryName,
                params,
                values);
    }
    
    @SuppressWarnings("unchecked")
    public List<T> getCurrentPageRows(final int first, final int pageSize, final String sortColumn, final boolean sortOrder, final Map<String, String> params) {
        return (List<T>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(persistentClass);

                if (params != null) {
                    for (String propertyName : params.keySet()) {
                        String value = params.get(propertyName);
                        criteria.add(Restrictions.eq(propertyName, value));
                    }
                }

                if (StringUtils.isNotBlank(sortColumn)) {
                    if (sortOrder) {
                        criteria.addOrder(Order.asc(sortColumn));
                    } else {
                        criteria.addOrder(Order.desc(sortColumn));
                    }
                }

                criteria.setFirstResult(first);
                criteria.setMaxResults(pageSize);
                return criteria.list();
            }
        });
    }

    public int getRowCount(final List<Criterion> params) {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(persistentClass)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .setProjection(Projections.rowCount());

                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        Criterion criterion = params.get(i);
                        criteria.add(criterion);
                    }
                }

                List result = criteria.list();
                Integer count = Integer.valueOf("0");
                if (result != null && result.size() > 0) {
                    count = (Integer) result.get(0);
                }
                return count;
            }
        });
    }

    private String criteriaToString(Criteria criteria)
    {
     String sql=null;

        try
        {
            CriteriaImpl c = (CriteriaImpl)criteria;
            SessionImpl s = (SessionImpl)c.getSession();
            SessionFactoryImplementor factory = (SessionFactoryImplementor)s.getSessionFactory();
            String[] implementors = factory.getImplementors( c.getEntityOrClassName() );
            CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable)factory.getEntityPersister(implementors[0]),
                factory, c, implementors[0], s.getEnabledFilters());
            Field f = OuterJoinLoader.class.getDeclaredField("sql");
            f.setAccessible(true);
            sql = (String)f.get(loader);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return sql;
    }

    public int getRowCount(final List<Criterion> params, final List<JoinPath> joinPaths) {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Integer count = Integer.valueOf("0");
                ProjectionList projectionList = Projections.projectionList();
                Criteria criteria = session.createCriteria(persistentClass) ;


                processJoinPath(criteria, joinPaths, projectionList);
                if (params != null) {
                    for (Criterion criterion : params)
                    {
                        criteria.add(criterion);
                    }
                }
                //jika projectionList tidak kosong berarti ada GROUPING, lakukan konversi criteria
                if(projectionList.getLength()>0)
                {
                    criteria.setProjection(projectionList);
                    String sql = criteriaToString(criteria);
                    sql = "select count(*) from ("+sql+") as countTemp";
                    //native SQL dijalankan
                    SQLQuery sqlQuery = session.createSQLQuery(sql);
                    List result = sqlQuery.list();
                    if (result != null && result.size() > 0) {
                        count = (Integer) result.get(0);
                    }

                }
                else
                {
                    criteria.setProjection(projectionList.add(Projections.rowCount()));
                    List result = criteria.list();
                    if (result != null && result.size() > 0) {
                        count = (Integer) result.get(0);
                    }
                }
                return count;
            }
        });
    }

    protected void processJoinPath(Criteria criteria, List<JoinPath> joinPaths, ProjectionList projectionList)
    {
        boolean isDistinctRoot = true;
        if(joinPaths!=null && !joinPaths.isEmpty())
        {
            for (JoinPath joinPath : joinPaths)
            {
                if(joinPath.joinType== JoinType.INNER_JOIN)
                    criteria.createCriteria(joinPath.path, joinPath.alias, CriteriaSpecification.INNER_JOIN);
                else if(joinPath.joinType== JoinType.LEFT_JOIN)
                    criteria.createCriteria(joinPath.path, joinPath.alias, CriteriaSpecification.LEFT_JOIN);
                else if(joinPath.joinType== JoinType.NON_DISTINCT_ROOT_ENTITY)
                    isDistinctRoot=false;
                else if(joinPath.joinType== JoinType.GROUPING_FIELD)
                {
                    if(projectionList==null)
                    {
                        projectionList = Projections.projectionList();
                    }
                    projectionList.add(Projections.groupProperty(joinPath.alias));
                }
                else
                    criteria.setFetchMode(joinPath.path, FetchMode.SELECT);

            }
        }
        if(isDistinctRoot)
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }


    /**
     * Search by criteria untuk property yang merefer ke tabel lain
     * misal : criteria nama identity untuk tabel license.
     * Tidak bisa menggunakan list<criterion> biasa seperti SearchByCriteria karena untuk property yang merefer ke
     * tabel lain, harus menggunakan criteria.createCriteria. See Hibernate dokumen untuk lebih jelas masalah ini
     *
     * criteria dibungkus dalam objek  org.ifc.municipal.dao.CriterionEntry.
     * CriterionEntry mempunyai dua entri, yaitu propertyName, dan criterion
     * propertyName adalah nama property anak, jika tidak merefer ke tabel lain, maka diisi null
     *
     * catatan : hanya bisa satu level
     *
     * @param orderList
     * @param firstResult
     * @param maxResults
     * @return
     * @author peter
     */
    public List<T> searchByPropertyCriteria(List<CriterionEntry> namedCriterionList,  List<OrderEntry> orderList, int firstResult, int maxResults){
        Criteria criteria = getSession().createCriteria(persistentClass);

        Criteria childCriteria = null;

        HashMap<String, Criteria> map = new HashMap<String,Criteria>();

        if ( namedCriterionList != null ){
            for (CriterionEntry namedCriteria : namedCriterionList) {
                if (namedCriteria.getPropertyName() == null) {
                    criteria.add(namedCriteria.getCriterion());
                } else {
                    childCriteria = map.get(namedCriteria.getPropertyName());

                    if (childCriteria == null) {
                        childCriteria = criteria.createCriteria(namedCriteria.getPropertyName());
                        map.put(namedCriteria.getPropertyName(), childCriteria);
                    }

                    childCriteria.add(namedCriteria.getCriterion());
                }
            }
        }

        if ( orderList != null ){
            for (OrderEntry order : orderList) {

                if (order.getPropertyName() == null) {
                    criteria.addOrder(order.getOrder());
                } else {
                    childCriteria = map.get(order.getPropertyName());

                    if (childCriteria == null) {
                        childCriteria = criteria.createCriteria(order.getPropertyName());
                        map.put(order.getPropertyName(), childCriteria);
                    }

                    childCriteria.addOrder(order.getOrder());
                }
            }
        }

        if ( firstResult >= 0 && maxResults > 0 ){
            criteria.setFirstResult(firstResult);
            criteria.setMaxResults(maxResults);
        }

        return criteria.list();
    }
    

    @SuppressWarnings("unchecked")
    public List<T> getSinglePage(final int firstRow, final int pageSize, final List<Criterion> params, final Order[] orders) {
        return (List<T>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(persistentClass)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);


                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        Criterion criterion = params.get(i);
                        criteria.add(criterion);
                    }
                }

                if (orders != null) {
                    for (int i = 0; i < orders.length; i++) {
                        Order order = orders[i];
                        criteria.addOrder(order);
                    }
                }

                criteria.setFirstResult(firstRow);
                criteria.setMaxResults(pageSize);                

                return criteria.list();
            }
        });
    }

    /**
     * Alternative getSinglePage, instead of extending Generic* class, try to map the entity alias in Join Path.<br>
     * Untuk criteria yang memiliki lebih dari satu level table gunakan method ini.
     * misal:<br><pre>
     *      <b>Restrictions.eq("tableA.tableB.tableC.properti", Long.parseLong(kodeCabang.getKodeBranch()))</b> 
     * </pre>
     * (lebih dari 1 level) tidak dapat dikenali oleh Hibernate criteria.
     * solusinya daftarkan path relasi table dgn JOIN PATH sehingga notasi criteria menjadi
     * <br>
     * <pre>
     *      <b>Restrictions.eq("tableAliasC.property", Long.parseLong(kodeCabang.getKodeBranch()))</b> 
     * </pre>
     * @see
     * @param firstRow
     * @param pageSize
     * @param joinPaths
     * @param params
     * @param orders
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<T> getSinglePage(final int firstRow, final int pageSize, final List<JoinPath> joinPaths, final List<Criterion> params, final Order[] orders) {
        return (List<T>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(persistentClass);
                        //.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

                processJoinPath(criteria, joinPaths, null);
                
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        Criterion criterion = params.get(i);
                        criteria.add(criterion);
                    }
                }

                if (orders != null) {
                    for (int i = 0; i < orders.length; i++) {
                        Order order = orders[i];
                        criteria.addOrder(order);
                    }
                }
                if(firstRow>=0)
                    criteria.setFirstResult(firstRow);
                if(pageSize>=0)
                    criteria.setMaxResults(pageSize);

                List returnedList = criteria.list();
                try
                {
                    recursiveInitialize(joinPaths, returnedList);
                }
                catch (OgnlException e)
                {
                    throw new RuntimeException(e.getMessage());
                }
                return returnedList;
            }
        });
    }

    /**
     * Retrive list dengan criteria dan join path yang diberikan
     * @param joinPaths
     * @param params
     * @param orders
     * @return
     */
    public List<T> getList(final List<JoinPath> joinPaths, final List<Criterion> params, final Order[] orders) {
        return getSinglePage(-1, -1, joinPaths, params, orders);
    }

    /**
     * mentrigger hibernate untuk melakukan extra query untuk lazy propertynya
     * berguna jika session sudah close, menghindari lazy initialization exception
     * ini berbeda dengan INNER/OUTER JoinType lho
     * @param joinPaths
     * @param rootObj
     */
    protected void recursiveInitialize(List<JoinPath> joinPaths, Object rootObj) throws OgnlException
    {

        Map<String, Object> joinPathMap = new HashMap<String, Object>();
        Map<String, Object> flatIndex = new HashMap<String, Object>();
        HashMap parentMap = new HashMap();
        parentMap.put("alias","this");
        parentMap.put("joinType", JoinType.LEFT_JOIN);
        joinPathMap.put("this", parentMap);
        flatIndex.put("this", parentMap);

        for (JoinPath joinPath : joinPaths)
        {
            if(StringUtils.isBlank(joinPath.alias))
            {
                //kalo kosong lewati
/*
                String[] pathArray = joinPath.path.split("[.]");
                HashMap mapMember = new HashMap();
                mapMember.put("joinType", joinPath.joinType);
                String key = pathArray[pathArray.length - 1];
                if(flatIndex.get(key)!=null)//ada alias kembar tolak !!
                {
                     throw new RuntimeException("Alias dari Join Path :"+key+" terdefinisi lebih dari sekali");
                }
                flatIndex.put(key, mapMember);
*/
            }
            else
            {
                HashMap mapMember = new HashMap();
                mapMember.put("joinType", joinPath.joinType);
                String key = joinPath.alias;
                if(flatIndex.get(key)!=null)//ada alias kembar tolak !!
                {
                     throw new RuntimeException("Alias dari Join Path :"+key+" terdefinisi lebih dari sekali");
                }
                flatIndex.put(key, mapMember);
            }
        }
        for (JoinPath joinPath : joinPaths)
        {
            String[] pathArray = joinPath.path.split("[.]");
            if(pathArray.length>1)
            {
                //gabung alias ke pathnya
                //cari parent
                Map mapParent = (Map) flatIndex.get(pathArray[0]);
                if(mapParent==null)
                    continue;
                //cari alias child
                Map mapChild;
                //ambil dari alias dari looping atas
                if(StringUtils.isNotBlank(joinPath.alias))
                {
                    mapChild = (Map) flatIndex.get(joinPath.alias);
                }
                else
                {
                    mapChild = (Map) flatIndex.get(pathArray[1]);
                }
                mapParent.put(pathArray[1], mapChild);
            }
            else
            {
                //gabung alias ke pathnya
                //cari parent -- this
                Map mapParent = (Map) flatIndex.get("this");
                if(mapParent==null)
                    continue;
                //cari alias child
                Map mapChild;
                //ambil dari alias dari looping atas
                if(StringUtils.isNotBlank(joinPath.alias))
                {
                    mapChild = (Map) flatIndex.get(joinPath.alias);
                }
                else
                {
                    mapChild = (Map) flatIndex.get(pathArray[0]);
                }
                mapParent.put(pathArray[0], mapChild);
            }
        }
        if(cleanUp((Map<String, Object>) joinPathMap.get("this")))
        {
            if (Collection.class.isAssignableFrom(rootObj.getClass()))
            {
                for (Object rootObjIter : ((Collection) rootObj))
                {
                    recursiveInitialize((Map<String, Object>) joinPathMap.get("this"), rootObjIter, true);
                }
            }
            else
            {
                recursiveInitialize((Map<String, Object>) joinPathMap.get("this"), rootObj, false);
            }
        }
    }

    private boolean cleanUp(Map<String, Object> joinPathMap)
    {
        if(joinPathMap==null)
            return false;
        //traverse sampai ke node
        List<String> deletedList = new ArrayList<String>();
        //check type
        JoinType joinType = (JoinType) joinPathMap.get("joinType");
        boolean returnValue = joinType.equals(JoinType.FETCH_MODE_SELECT);
        for (String key : joinPathMap.keySet())
        {
            if(key.equals("alias") || key.equals("joinType"))
                continue;

            Map mapChild = (Map) joinPathMap.get(key);
            //hanya sisakan yang fetch select
            if(!cleanUp(mapChild))
                deletedList.add(key);
            else
                returnValue = true;
        }
        for (String key : deletedList)
        {
            joinPathMap.remove(key);
        }
        return returnValue;
    }

    private void recursiveInitialize(Map<String, Object> joinPath, Object parentObject, boolean isCollection) throws OgnlException
    {
        for (String key : joinPath.keySet())
        {
            if (key.equals("alias") || key.equals("joinType"))
                continue;

            if (!isCollection && Collection.class.isAssignableFrom(parentObject.getClass()))
            {
                Map mapChild = (Map) joinPath.get(key);
                for (Object objIter : ((Collection) parentObject))
                {
                    recursiveInitialize(mapChild, objIter, true);
                }
            }
            else
            {
                Object prop = null;
                try
                {
                    prop = Ognl.getValue(key, parentObject);
                    Hibernate.initialize(prop);
                    Map mapChild = (Map) joinPath.get(key);
                    recursiveInitialize(mapChild, prop, false);
                }
                catch (Exception e)
                {
                    //not error
                }
            }
        }

    }

    public List<T> searchByCriteria(SearchCriteria searchCriteria, final int firstResult, final int maxResults){
        Criteria criteria = constuctCriteria(searchCriteria, true);
        log.info("search criteria "+firstResult+" "+maxResults);

        if ( firstResult >= 0 ){
            log.info("x1");
            criteria.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            log.info("x2");
            criteria.setMaxResults(10);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List list = criteria.list();
        log.info("hasil = "+list.size());
        return list;
    }

    public Integer searchByCriteriaCount(SearchCriteria searchCriteria){
        Criteria criteria = constuctCriteria(searchCriteria, false);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());

        return (Integer) criteria.uniqueResult();
    }
    

    protected Criteria constuctCriteria(SearchCriteria searchCriteria, boolean withOrder)
    {
        //first level citeria must current persistentClass
        Criteria criteria = getSession().createCriteria(persistentClass);
        if (searchCriteria == null){
            return criteria;
        }
        constuctCriteria(searchCriteria, criteria, withOrder);
        return criteria;
    }

    private void constuctCriteria(SearchCriteria searchCriteria, Criteria criteria, boolean withOrder)
    {
        if ( searchCriteria.getCriterionList() != null ){
            for (Criterion criterion : searchCriteria.getCriterionList()) {
                criteria.add(criterion);
            }
        }

        if ( withOrder && searchCriteria.getOrderList() != null ){
            for (Order order : searchCriteria.getOrderList()) {
                criteria.addOrder(order);
            }
        }

        if (searchCriteria.getSubSearchCriteriaList() != null){
            for (SearchCriteria subSearchCriteria : searchCriteria.getSubSearchCriteriaList()) {
                Criteria subCriteria = null;
                if ( subSearchCriteria.getJoinType() == CriteriaSpecification.INNER_JOIN ){
                    subCriteria = criteria.createCriteria(subSearchCriteria.getEntityName());
                } else {
                    subCriteria = criteria.createCriteria(subSearchCriteria.getEntityName(), subSearchCriteria.getJoinType());
                }

                constuctCriteria(subSearchCriteria, subCriteria, withOrder);
            }
        }
    }

    public void clearCache(){
        getSession().clear();
    }
    /*
    public static void main(String[] args)
    {
        List<JoinPath>list = new ArrayList<JoinPath>();
        list.add(new GenericDao.JoinPath("this.obligasiId", "obligasi", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("this.noAccountSumber", "accountBank", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("obligasi.kodeCabang", "cabang", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("obligasi.kodeObligasi", "masterObligasi", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("this.currOriginal", "currency", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("obligasi.kodeFundManager", "fundManager", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("obligasi.kodeJenisInvestasi", "parameter", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("obligasi.pencairanObligasiList", "pencairan", GenericDao.JoinType.LEFT_JOIN));

        list.add(new GenericDao.JoinPath("masterObligasi.emiten", "emiten", GenericDao.JoinType.LEFT_JOIN));
        list.add(new GenericDao.JoinPath("accountBank.bank", "bank", GenericDao.JoinType.LEFT_JOIN));

        list.add(new GenericDao.JoinPath("pencairan.currNominal", "currencyCair", GenericDao.JoinType.LEFT_JOIN));
        GenericDaoHibernate genericDaoHibernate = new GenericDaoHibernate(Customer.class);
        try
        {
            genericDaoHibernate.recursiveInitialize(list, new Customer());
        }
        catch (OgnlException e)
        {
            e.printStackTrace();
        }
    }*/
}
