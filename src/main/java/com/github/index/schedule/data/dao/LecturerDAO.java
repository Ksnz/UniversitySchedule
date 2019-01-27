package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Lecturer;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;

public class LecturerDAO extends AbstractDAO<Lecturer, Integer> {

    private static final Logger LOGGER = Logger.getLogger(LecturerDAO.class);

    public LecturerDAO(EntityManager entityManager) {
        super(entityManager);
    }

    public LecturerDAO() {
    }

    public long count() {
        try {
            return super.count(Lecturer.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех преподавателей из бд", e);
        }
        return 0;
    }

    @Override
    public List<Lecturer> findAll() {
        try {
            return findAllByClass(Lecturer.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех преподавателей из бд", e);
        }
        return Collections.emptyList();
    }

    public List<Lecturer> findIn(int start, int end) {
        try {
            return entityManager.createQuery("SELECT l FROM Lecturer l ORDER BY l.lecturerId").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса преподавателей по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Lecturer> find(Integer key) {
        try {
            return Optional.ofNullable(findByKey(Lecturer.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса преподавателя из бд по ключу", e);
        }
        return Optional.empty();
    }

    public void createLecturer(String firstName, String lastName, String patronymic, LocalDate birthDay) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Lecturer lecturer = new Lecturer();
            lecturer.setFirstName(firstName);
            lecturer.setLastName(lastName);
            lecturer.setPatronymic(patronymic);
            lecturer.setBirthDay(birthDay);
            entityManager.persist(lecturer);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания студента в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateLecturer(Lecturer lecturer, int lecturerNumber, String firstName, String lastName, String patronymic, LocalDate birthDay) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            lecturer.setLecturerId(lecturerNumber);
            lecturer.setFirstName(firstName);
            lecturer.setLastName(lastName);
            lecturer.setPatronymic(patronymic);
            lecturer.setBirthDay(birthDay);
            entityManager.merge(lecturer);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления преподавателя в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Lecturer lecturer) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(lecturer);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления преподавателя в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void put(Lecturer lecturer) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(lecturer);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления преподавателя из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteLecturer(Lecturer lecturer) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(lecturer);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления преподавателя из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public Optional<Lecturer> findBy(String firstName, String lastName, String patronymic) {
        try {
            Query query = entityManager.createQuery("SELECT l FROM Lecturer l WHERE lower(l.firstName) like :firstName AND lower(l.lastName)like :lastName AND lower(l.patronymic) like :patronymic");
            query.setParameter("firstName", firstName.toLowerCase());
            query.setParameter("lastName", lastName.toLowerCase());
            query.setParameter("patronymic", patronymic.toLowerCase());
            return Optional.of((Lecturer) query.getSingleResult());
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса групп по диапазону из бд", e);
        }
        return Optional.empty();
    }
}
