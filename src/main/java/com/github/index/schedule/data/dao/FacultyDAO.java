package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Faculty;
import org.apache.log4j.Logger;

import javax.ejb.Singleton;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
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
            return entityManager.createQuery("SELECT f FROM Faculty f ORDER BY f.id").setFirstResult(start).setMaxResults(end).getResultList();
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

            Faculty faculty = new Faculty();
            faculty.setId(id);
            faculty.setShortName(shortName);
            faculty.setFullName(fullName);
            entityManager.persist(faculty);
    }

    public void updateFaculty(Faculty faculty, char id, String shortName, String fullName) {

            faculty.setId(id);
            faculty.setShortName(shortName);
            faculty.setFullName(fullName);
            entityManager.merge(faculty);
    }
}
