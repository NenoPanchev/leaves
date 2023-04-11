package com.example.leaves.model.dto;

public class PdfRequestForm {

    private String requestToName;
    private String year;
    private String position;
    private String location;
    private String egn;

    public String getRequestToName() {
        return requestToName;
    }

    public void setRequestToName(String requestToName) {
        this.requestToName = requestToName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEgn() {
        return egn;
    }

    public void setEgn(String egn) {
        this.egn = egn;
    }
}
