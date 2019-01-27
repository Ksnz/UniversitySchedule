package com.github.index.schedule.data.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T, K> implements BaseDAO<T, K> {

    static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");

    final private ThreadLocal<EntityManager> threadLocal = ThreadLocal.withInitial(entityManagerFactory::createEntityManager);

    protected long count(Class<T> tClass) {
        return (long) getEntityManager().createQuery("SELECT COUNT(t) FROM " + tClass.getSimpleName() + " t").getSingleResult();
    }

    public EntityManager getEntityManager() {
        return threadLocal.get();
    }

    protected List<T> findAllByClass(Class<T> tClass) {
        return getEntityManager().createQuery("SELECT t FROM " + tClass.getSimpleName() + " t").getResultList();
    }

    protected T findByKey(Class<T> tClass, K key) {
        return getEntityManager().find(tClass, key);
    }

    public abstract Optional<T> find(K key);

    public abstract void update(T t);

}
