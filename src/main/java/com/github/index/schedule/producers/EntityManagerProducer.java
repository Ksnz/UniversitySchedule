package com.github.index.schedule.producers;

import javax.ejb.Singleton;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;

@Singleton
public class EntityManagerProducer {

    @PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private javax.persistence.EntityManagerFactory entityManagerFactory;

    @Produces
    @RequestScoped
    public EntityManager create() {
        return entityManagerFactory.createEntityManager();
    }

    public void close(@Disposes EntityManager em) {
        em.close();
    }
}
