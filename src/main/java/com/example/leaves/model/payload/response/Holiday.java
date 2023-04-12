package com.example.leaves.model.payload.response;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.time.LocalDate;
@Data
public class Holiday {
    @Expose
    private LocalDate date;
    @Expose
    private String name;
}
