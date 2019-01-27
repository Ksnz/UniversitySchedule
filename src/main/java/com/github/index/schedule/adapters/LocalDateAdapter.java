package com.github.index.schedule.adapters;

import lombok.NonNull;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd").toFormatter();

    public LocalDate unmarshal(@NonNull String v) throws Exception {
        return LocalDate.parse(v, formatter);
    }

    public String marshal(@NonNull LocalDate v) throws Exception {
        return v.format(formatter);
    }
}
