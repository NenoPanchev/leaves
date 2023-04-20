package com.example.leaves.controller;

import com.example.leaves.model.dto.DepartmentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
@CrossOrigin
@RequestMapping("/api/dates")
public interface DatesController {
    @GetMapping
    ResponseEntity<List<LocalDate>> getAllHolidays();
}
