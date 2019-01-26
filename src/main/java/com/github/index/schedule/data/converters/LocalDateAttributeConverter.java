package com.github.index.schedule.data.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd").toFormatter();

    @Override
    public String convertToDatabaseColumn(LocalDate locDate) {
        return (locDate == null ? null : locDate.format(formatter));
    }

    @Override
    public LocalDate convertToEntityAttribute(String sqlDate) {
        return (sqlDate == null ? null : LocalDate.parse(sqlDate, formatter));
    }
}