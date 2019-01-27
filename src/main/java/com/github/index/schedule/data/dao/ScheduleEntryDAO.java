package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.*;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;


public class ScheduleEntryDAO extends AbstractDAO<ScheduleEntry, Integer> {

    private static final Logger LOGGER = Logger.getLogger(ScheduleEntryDAO.class);

    public ScheduleEntryDAO(EntityManager entityManager) {
        super(entityManager);
    }

    public ScheduleEntryDAO() {
    }

    public long count() {
        try {
            return super.count(ScheduleEntry.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех расписаний из бд", e);
        }
        return 0;
    }

    @Override
    public List<ScheduleEntry> findAll() {
        try {
            return findAllByClass(ScheduleEntry.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех расписаний из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<ScheduleEntry> find(Integer key) {
        try {
            return Optional.ofNullable(findByKey(ScheduleEntry.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса расписания из бд по ключу", e);
        }
        return Optional.empty();
    }

    public List<ScheduleEntry> findForGroup(Group group) {
        try {
            Query query = entityManager.createQuery("SELECT e FROM ScheduleEntry e JOIN e.groups g WHERE g = :g ORDER BY e.id");
            query.setParameter("g", group);
            return query.getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса расписания из бд по номеру группы", e);
        }
        return Collections.emptyList();
    }

    public List<ScheduleEntry> findForLecturer(Lecturer lecturer) {
        try {
            Query query = entityManager.createQuery("SELECT e FROM ScheduleEntry e WHERE e.lecturer = :l ORDER BY e.id");
            query.setParameter("l", lecturer);
            return query.getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса расписания из бд по номеру группы", e);
        }
        return Collections.emptyList();
    }

    public List<ScheduleEntry> findIn(int start, int end) {
        try {
            return entityManager.createQuery("SELECT e FROM ScheduleEntry e ORDER BY e.id").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса расписаний по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createScheduleEntry(Auditorium auditorium, Course course, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, byte weekNumber, Set<Group> groups, Lecturer lecturer) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            ScheduleEntry scheduleEntry = new ScheduleEntry();
            fill(scheduleEntry, course, dayOfWeek, auditorium, endTime, startTime, groups, lecturer, weekNumber);
            entityManager.persist(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            LOGGER.error("Ошибка создания расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    private void fill(ScheduleEntry scheduleEntry, Course course, DayOfWeek dayOfWeek, Auditorium auditorium, LocalTime endTime, LocalTime startTime, Set<Group> groups, Lecturer lecturer, byte weekNumber) {
        scheduleEntry.setAuditorium(auditorium);
        scheduleEntry.setCourse(course);
        scheduleEntry.setDayOfWeek(dayOfWeek);
        scheduleEntry.setStartTime(startTime);
        scheduleEntry.setGroups(groups);
        scheduleEntry.setEndTime(endTime);
        scheduleEntry.setWeekNumber(weekNumber);
        scheduleEntry.setLecturer(lecturer);
    }

    public void updateScheduleEntry(ScheduleEntry scheduleEntry, Auditorium auditorium, Course course, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, byte weekNumber, Set<Group> groups, Lecturer lecturer) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            fill(scheduleEntry, course, dayOfWeek, auditorium, endTime, startTime, groups, lecturer, weekNumber);
            entityManager.merge(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void addGroup(ScheduleEntry scheduleEntry, Group group) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            scheduleEntry.getGroups().add(group);
            entityManager.merge(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void addGroups(ScheduleEntry scheduleEntry, Collection<Group> group) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            scheduleEntry.getGroups().addAll(group);
            entityManager.merge(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(ScheduleEntry scheduleEntry) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }


    public void put(ScheduleEntry scheduleEntry) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления расписания в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteScheduleEntry(ScheduleEntry scheduleEntry) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(scheduleEntry);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления расписания из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
