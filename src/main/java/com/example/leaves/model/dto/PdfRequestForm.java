package com.example.leaves.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdfRequestForm {

    private String requestToName;
    private String year;
    private String position;
    private String address;
    private char[] ssn;

    private boolean saved;

}
