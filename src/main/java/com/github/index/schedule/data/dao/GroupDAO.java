package com.github.index.schedule.data.dao;

import com.github.index.schedule.data.entity.Faculty;
import com.github.index.schedule.data.entity.Group;
import org.apache.log4j.Logger;

import javax.ejb.Singleton;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
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
            return entityManager.createQuery("SELECT g FROM Group g ORDER BY g.groupId").setFirstResult(start).setMaxResults(end).getResultList();
        } catch (PersistenceException e) {
            LOGGER.error("Ошибка запроса групп по диапазону из бд", e);
        }
        return Collections.emptyList();
    }

    public void createGroup(int id, Faculty faculty) {
        Group group = new Group();
        group.setGroupId(id);
        group.setFaculty(faculty);
        faculty.getGroups().add(group);
        entityManager.persist(group);
        entityManager.merge(faculty);
    }

    public void updateGroup(Group group, int id, Faculty faculty) {
        group.setGroupId(id);
        group.setFaculty(faculty);
        faculty.getGroups().add(group);
        entityManager.merge(group);
        entityManager.merge(faculty);
    }
}
