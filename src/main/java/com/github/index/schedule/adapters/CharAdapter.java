package com.github.index.schedule.adapters;

import lombok.NonNull;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CharAdapter extends XmlAdapter<String, Character> {

    @Override
    public String marshal(@NonNull Character v) throws Exception {
        return new String(new char[]{v});
    }

    @Override
    public Character unmarshal(@NonNull String v) throws Exception {
        if (v.isEmpty()) {
            throw new IllegalArgumentException("String cannot be empty");
        }
        return v.charAt(0);
    }

}