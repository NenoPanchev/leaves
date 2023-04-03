package com.example.leaves.exceptions;

public class PaidleaveNotEnoughException extends BaseCustomException {
    public PaidleaveNotEnoughException(int requestedDays, int annualLeaveLeft) {
        super(String.format("Requested %d days leave. You have %d left.", requestedDays, annualLeaveLeft));
    }

    public PaidleaveNotEnoughException(String formattedString) {
        super(formattedString);
    }

    public PaidleaveNotEnoughException(String formattedString, String type) {
        super(formattedString, type);
    }

}
