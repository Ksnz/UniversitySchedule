package com.github.index.schedule.data.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm").toFormatter();

    public LocalTime unmarshal(String v) throws Exception {
        return LocalTime.parse(v, formatter);
    }

    public String marshal(LocalTime v) throws Exception {
        return v.format(formatter);
    }
}
