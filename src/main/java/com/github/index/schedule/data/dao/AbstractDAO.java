package com.github.index.schedule.data.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;


public abstract class AbstractDAO<T, K> implements BaseDAO<T, K> {

    @Inject
    EntityManager entityManager;

    protected long count(Class<T> tClass) {
        return (long) entityManager.createQuery("SELECT COUNT(t) FROM " + tClass.getSimpleName() + " t").getSingleResult();
    }

    protected List<T> findAllByClass(Class<T> tClass) {
        return entityManager.createQuery("SELECT t FROM " + tClass.getSimpleName() + " t").getResultList();
    }

    protected T findByKey(Class<T> tClass, K key) {
        return entityManager.find(tClass, key);
    }

    public abstract Optional<T> find(K key);

    public void update(T t) {
        entityManager.merge(t);
    }

    public void put(T t) {
        entityManager.persist(t);
    }

    public void delete(T t) {
        entityManager.remove(entityManager.merge(t));
    }
}
