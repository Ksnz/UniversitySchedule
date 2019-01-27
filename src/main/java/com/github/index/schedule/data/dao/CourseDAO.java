package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Course;
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
public class CourseDAO extends AbstractDAO<Course, Integer> {
    private static final Logger LOGGER = Logger.getLogger(CourseDAO.class);

    public long count() {
        try {
            return super.count(Course.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех предметов из бд", e);
        }
        return 0;
    }

    @Override
    public List<Course> findAll() {
        try {
            return findAllByClass(Course.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех предметов из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Course> find(Integer key) {
        try {
            return Optional.ofNullable(findByKey(Course.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса предмета из бд по ключу", e);
        }
        return Optional.empty();
    }

    public List<Course> findIn(int start, int end) {
        try {
            return getEntityManager().createQuery("SELECT с FROM Course с ORDER BY с.id").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса предметов по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createCourse(int id, String shortName, String fullName) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            Course course = new Course();
            course.setId(id);
            course.setShortName(shortName);
            course.setFullName(fullName);
            getEntityManager().persist(course);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания предмета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateCourse(Course course, int id, String shortName, String fullName) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            course.setId(id);
            course.setShortName(shortName);
            course.setFullName(fullName);
            getEntityManager().merge(course);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления предмета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Course course) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().merge(course);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления предмета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void put(Course course) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().persist(course);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления предмета из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteCourse(Course course) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().remove(course);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления предмета из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
