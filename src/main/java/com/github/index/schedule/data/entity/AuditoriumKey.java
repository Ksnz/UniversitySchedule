package com.github.index.schedule.data.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuditoriumKey implements Serializable {
    private int room;
    private int housing;
}
