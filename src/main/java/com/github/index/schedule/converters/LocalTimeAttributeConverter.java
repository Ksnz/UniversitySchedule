package com.github.index.schedule.converters;

import lombok.NonNull;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm").toFormatter();

    @Override
    public String convertToDatabaseColumn(@NonNull LocalTime locTime) {
        return locTime.format(formatter);
    }

    @Override
    public LocalTime convertToEntityAttribute(@NonNull String sqlTime) {
        return LocalTime.parse(sqlTime, formatter);
    }
}