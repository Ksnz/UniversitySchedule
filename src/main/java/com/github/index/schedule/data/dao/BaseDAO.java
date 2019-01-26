package com.github.index.schedule.data.dao;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T, K> {
    List<T> findAll();

    Optional<T> find(@NonNull K key);
}