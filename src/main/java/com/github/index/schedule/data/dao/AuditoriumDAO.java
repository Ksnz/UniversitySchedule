package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Auditorium;
import com.github.index.schedule.data.entity.AuditoriumKey;
import org.apache.log4j.Logger;

import javax.ejb.Singleton;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
public class AuditoriumDAO extends AbstractDAO<Auditorium, AuditoriumKey> {

    private static final Logger LOGGER = Logger.getLogger(AuditoriumDAO.class);

    public long count() {
        try {
            return super.count(Auditorium.class);
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
            LOGGER.error("Ошибка запроса из бд", e);
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
            return entityManager.createQuery("SELECT a FROM Auditorium a ORDER BY a.housing, a.room").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса аудиторий по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createAuditorium(int room, int housing, int capacity) {
        Auditorium auditorium = new Auditorium();
        auditorium.setRoom(room);
        auditorium.setHousing(housing);
        auditorium.setCapacity(capacity);
        entityManager.persist(auditorium);
    }

    public void updateAuditorium(Auditorium auditorium, int capacity) {
        auditorium.setCapacity(capacity);
        entityManager.merge(auditorium);
    }
}
