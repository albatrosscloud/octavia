package pe.albatross.octavia.easydao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;
import com.google.common.base.Preconditions;
import pe.albatross.octavia.Octavia;
import pe.albatross.octavia.dynatable.DynatableSql;

@SuppressWarnings("unchecked")
public abstract class AbstractEasyDAO<T extends Serializable> implements EasyDAO<T> {

    private Class<T> clazz;

    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected void setClazz(final Class<T> clazzToSet) {
        clazz = Preconditions.checkNotNull(clazzToSet);
    }

    @Override
    public T find(final long id) {
        return (T) getCurrentSession().get(clazz, id);
    }

    protected T find(Octavia octavia) {
        return (T) octavia.find(this.getCurrentSession());
    }

    protected T findEvict(Octavia octavia) {
        T objeto = (T) octavia.find(this.getCurrentSession());

        if (objeto != null) {
            this.getCurrentSession().evict(objeto);
        }
        return objeto;
    }

    @Override
    public List<T> all() {
        String query = "from " + clazz.getName();
        return getCurrentSession().createQuery(query).list();
    }

    protected List<T> all(Octavia octavia) {
        return octavia.all(this.getCurrentSession());
    }

    protected List<T> all(DynatableSql dynatableSql) {
        return dynatableSql.all(this.getCurrentSession());
    }

    @Override
    public List<T> all(List<Long> ids) {
        Query query = getCurrentSession().createQuery("FROM " + clazz.getName() + " c WHERE c.id IN :ids");
        query.setParameterList("ids", ids);
        return query.list();
    }

    @Override
    public void save(final T entity) {
        Preconditions.checkNotNull(entity);
        getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void update(final T entity) {
        Preconditions.checkNotNull(entity);
        getCurrentSession().merge(entity);
    }

    protected void update(Octavia octavia) {
        octavia.execute(this.getCurrentSession());
    }

    @Override
    public void updateColumns(T entity, String... columns) {
        Octavia sql = Octavia.update(clazz);
        sql.set(entity, columns);
        this.update(sql);
    }

    @Override
    public void delete(final T entity) {
        Preconditions.checkNotNull(entity);
        getCurrentSession().delete(entity);
    }

    @Override
    public void delete(final long entityId) {
        final T entity = find(entityId);
        Preconditions.checkState(entity != null);
        delete(entity);
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
