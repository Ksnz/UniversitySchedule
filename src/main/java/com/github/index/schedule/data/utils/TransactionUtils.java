package com.github.index.schedule.data.utils;

import org.apache.log4j.Logger;

import javax.persistence.EntityTransaction;

public class TransactionUtils {
    private static final Logger LOGGER = Logger.getLogger(TransactionUtils.class);

    private TransactionUtils() {
    }

    public static void rollBackSilently(EntityTransaction transaction) {
        try {
            transaction.rollback();
        } catch (Exception e) {
            LOGGER.error("RollBack failed", e);
        }
    }
}
