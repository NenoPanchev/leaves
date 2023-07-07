package com.example.leaves.controller.impl;

import com.example.leaves.controller.DatesController;
import com.example.leaves.util.HolidaysUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DatesControllerImpl implements DatesController {
    private final HolidaysUtil holidaysUtil;

    public DatesControllerImpl(HolidaysUtil holidaysUtil) {
        this.holidaysUtil = holidaysUtil;
    }

    @Override
    public ResponseEntity<List<LocalDate>> getAllHolidays() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(holidaysUtil.getHolidays());
    }
}
