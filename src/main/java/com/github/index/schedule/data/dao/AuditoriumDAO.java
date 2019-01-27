package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Auditorium;
import com.github.index.schedule.data.entity.AuditoriumKey;
import org.apache.log4j.Logger;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;

@Named
@ApplicationScoped
public class AuditoriumDAO extends AbstractDAO<Auditorium, AuditoriumKey> {

    private static final Logger LOGGER = Logger.getLogger(AuditoriumDAO.class);

    public long count() {
        try {
            return (long) getEntityManager().createQuery("SELECT COUNT(t) FROM Auditorium t").getSingleResult(); //fixme
            //return super.count(Auditorium.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех аудиторий из бд", e);
        }
        return 0;
    }

    @Override
    public List<Auditorium> findAll() {
        try {
            return findAllByClass(Auditorium.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех аудиторий из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Auditorium> find(AuditoriumKey key) {
        try {
            return Optional.ofNullable(findByKey(Auditorium.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса аудитории из бд по ключу", e);
        }
        return Optional.empty();
    }

    public List<Auditorium> findIn(int start, int end) {
        try {
            return getEntityManager().createQuery("SELECT a FROM Auditorium a ORDER BY a.housing, a.room").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса аудиторий по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createAuditorium(int room, int housing, int capacity) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            Auditorium auditorium = new Auditorium();
            auditorium.setRoom(room);
            auditorium.setHousing(housing);
            auditorium.setCapacity(capacity);
            getEntityManager().persist(auditorium);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания аудитории в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateAuditorium(Auditorium auditorium, int capacity) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            auditorium.setCapacity(capacity);
            getEntityManager().merge(auditorium);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления аудитории в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Auditorium auditorium) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().merge(auditorium);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления аудитории в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void put(Auditorium auditorium) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().persist(auditorium);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления аудитории в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteAuditorium(Auditorium auditorium) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().remove(auditorium);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления аудитории из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
