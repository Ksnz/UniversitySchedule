package com.github.index.schedule.data.converters;

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
    public String convertToDatabaseColumn(LocalTime locTime) {
        return (locTime == null ? null : locTime.format(formatter));
    }

    @Override
    public LocalTime convertToEntityAttribute(String sqlTime) {
        return (sqlTime == null ? null : LocalTime.parse(sqlTime, formatter));
    }
}