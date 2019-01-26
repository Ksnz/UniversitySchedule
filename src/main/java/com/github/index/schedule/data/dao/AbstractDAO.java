package com.github.index.schedule.data.dao;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T, K> implements BaseDAO<T, K> {

    //@PersistenceContext(unitName = "SchedulePersistenceUnit")
    protected EntityManager entityManager;

    public AbstractDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AbstractDAO() {
    }

    protected long count(Class<T> tClass) {
        return (long) entityManager.createQuery("SELECT COUNT(t) FROM " + tClass.getSimpleName() + " t").getSingleResult();
    }


    protected List<T> findAllByClass(Class<T> tClass) {
        return entityManager.createQuery("SELECT t FROM " + tClass.getSimpleName() + " t").getResultList();
    }

    protected T fingByKey(Class<T> tClass, K key) {
        return entityManager.find(tClass, key);
    }

    public abstract Optional<T> find(K key);

    public abstract void update(T t);

}
