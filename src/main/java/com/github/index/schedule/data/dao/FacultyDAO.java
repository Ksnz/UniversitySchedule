package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Faculty;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;

@Named
@ApplicationScoped
public class FacultyDAO extends AbstractDAO<Faculty, Character> {
    private static final Logger LOGGER = Logger.getLogger(FacultyDAO.class);

    public long count() {
        try {
            return super.count(Faculty.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех факультетов из бд", e);
        }
        return 0;
    }

    @Override
    public List<Faculty> findAll() {
        try {
            return findAllByClass(Faculty.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех факультетов из бд", e);
        }
        return Collections.emptyList();
    }

    public List<Faculty> findIn(int start, int end) {
        try {
            return getEntityManager().createQuery("SELECT f FROM Faculty f ORDER BY f.id").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса факультетов из бд по диапазону", e);
        }
        return Collections.emptyList();
    }


    @Override
    public Optional<Faculty> find(Character key) {
        try {
            return Optional.ofNullable(findByKey(Faculty.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса факультета из бд по ключу", e);
        }
        return Optional.empty();
    }

    public void createFaculty(char id, String shortName, String fullName) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            Faculty faculty = new Faculty();
            faculty.setId(id);
            faculty.setShortName(shortName);
            faculty.setFullName(fullName);
            getEntityManager().persist(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания факультета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateFaculty(Faculty faculty, char id, String shortName, String fullName) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            faculty.setId(id);
            faculty.setShortName(shortName);
            faculty.setFullName(fullName);
            getEntityManager().merge(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления факультета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Faculty faculty) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().merge(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления факультета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void put(Faculty faculty) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().persist(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления факультета из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteFaculty(Faculty faculty) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().remove(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления факультета из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
