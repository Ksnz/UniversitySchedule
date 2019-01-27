package com.github.index.schedule.adapters;

import lombok.NonNull;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm").toFormatter();

    public LocalTime unmarshal(@NonNull String v) throws Exception {
        return LocalTime.parse(v, formatter);
    }

    public String marshal(@NonNull LocalTime v) throws Exception {
        return v.format(formatter);
    }
}
