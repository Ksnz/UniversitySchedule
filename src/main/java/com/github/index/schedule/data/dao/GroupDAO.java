package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Faculty;
import com.github.index.schedule.data.entity.Group;
import org.apache.log4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.TransactionUtils.rollBackSilently;

@Singleton
@Startup
@Lock(LockType.READ)
public class GroupDAO extends AbstractDAO<Group, Integer> {
    private static final Logger LOGGER = Logger.getLogger(GroupDAO.class);

    public long count() {
        try {
            return super.count(Group.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса числа всех групп из бд", e);
        }
        return 0;
    }

    @Override
    public List<Group> findAll() {
        try {
            return findAllByClass(Group.class);
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса всех групп из бд", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Group> find(Integer key) {
        try {
            return Optional.ofNullable(findByKey(Group.class, key));
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса группы из бд по ключу", e);
        }
        return Optional.empty();
    }

    public List<Group> findIn(int start, int end) {
        try {
            return getEntityManager().createQuery("SELECT g FROM Group g ORDER BY g.groupId").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса групп по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createGroup(int id, Faculty faculty) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            Group group = new Group();
            group.setGroupId(id);
            group.setFaculty(faculty);
            faculty.getGroups().add(group);
            getEntityManager().persist(group);
            getEntityManager().merge(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка создания группы в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void updateGroup(Group group, int id, Faculty faculty) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            group.setGroupId(id);
            group.setFaculty(faculty);
            faculty.getGroups().add(group);
            getEntityManager().merge(group);
            getEntityManager().merge(faculty);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления группы в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void update(Group group) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().merge(group);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка обновления группы в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void put(Group group) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().persist(group);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка добавления группы в бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

    public void deleteGroup(Group group) {
        EntityTransaction transaction = getEntityManager().getTransaction();
        try {
            transaction.begin();
            getEntityManager().remove(group);
            transaction.commit();
        } catch (Throwable throwable) {
            LOGGER.error("Ошибка удаления группы из бд", throwable);
            rollBackSilently(transaction);
            throw new RuntimeException(throwable);
        }
    }

}
