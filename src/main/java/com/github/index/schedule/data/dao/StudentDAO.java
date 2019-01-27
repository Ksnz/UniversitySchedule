package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Group;
import com.github.index.schedule.data.entity.Student;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;

@Named
@ApplicationScoped
public class StudentDAO extends AbstractDAO<Student,Integer> {

    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class);

    public long count() {
        try {
            return super.count(Student.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех студентов из бд", e);
        }
        return 0;
    }

    @Override
    public List<Student> findAll() {
        try {
            return findAllByClass(Student.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех студентов из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Student> find(Integer key) {
        try {
            return Optional.ofNullable(findByKey(Student.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса студента из бд по ключу", e);
        }
        return Optional.empty();
    }

    public List<Student> findIn(int start, int end) {
        try {
            return getEntityManager().createQuery("SELECT s FROM Student s ORDER BY s.studentId").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса студентов по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createStudent(int studentNumber, String firstName, String lastName, String patronymic, LocalDate birthDay, Group group) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            Student student = new Student();
            student.setStudentId(studentNumber);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setPatronymic(patronymic);
            student.setGroup(group);
            student.setBirthDay(birthDay);
            group.getStudents().add(student);
            getEntityManager().persist(student);
            getEntityManager().merge(group);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания студента в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateStudent(Student student, int studentNumber, String firstName, String lastName, String patronymic, LocalDate birthDay, Group group) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            student.setStudentId(studentNumber);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setPatronymic(patronymic);
            student.getGroup().getStudents().remove(student);
            student.setGroup(group);
            student.setBirthDay(birthDay);
            group.getStudents().add(student);
            getEntityManager().merge(student);
            getEntityManager().merge(group);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления студента в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Student student) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().merge(student);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления факультета в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }


    public void put(Student student) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().persist(student);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления факультета из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteStudent(Student student) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().remove(student);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления студента из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
