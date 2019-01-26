package com.github.index.schedule.data.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CharAdapter extends XmlAdapter<String, Character> {

    @Override
    public String marshal(Character v) throws Exception {
        if (v == null) {
            throw new IllegalArgumentException("Character cannot be null");
        }
        return new String(new char[]{v});
    }

    @Override
    public Character unmarshal(String v) throws Exception {
        if (v == null || v.isEmpty()) {
            throw new IllegalArgumentException("String cannot be null or empty");
        }
        return v.charAt(0);
    }

}