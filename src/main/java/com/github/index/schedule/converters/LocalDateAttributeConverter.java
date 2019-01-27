package com.github.index.schedule.converters;

import lombok.NonNull;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd").toFormatter();

    @Override
    public String convertToDatabaseColumn(@NonNull LocalDate locDate) {
        return locDate.format(formatter);
    }

    @Override
    public LocalDate convertToEntityAttribute(@NonNull String sqlDate) {
        return LocalDate.parse(sqlDate, formatter);
    }
}