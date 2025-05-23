package com.ucapital.sharkshub.investor.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class InvestorValidationException extends RuntimeException {


    private final List<ValidationError> validationErrors;


    private final Integer investorIndex;


    private final String investorName;


    public InvestorValidationException(String message, Integer investorIndex,
                                       String investorName, List<ValidationError> validationErrors) {
        super(message);
        this.investorIndex = investorIndex;
        this.investorName = investorName;
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }


    public InvestorValidationException(String message, String fieldName, Object rejectedValue,
                                       Integer investorIndex, String investorName) {
        super(message);
        this.investorIndex = investorIndex;
        this.investorName = investorName;
        this.validationErrors = List.of(
                ValidationError.builder()
                        .fieldName(fieldName)
                        .rejectedValue(rejectedValue)
                        .errorMessage(message)
                        .build()
        );
    }


    public InvestorValidationException(String message, Throwable cause) {
        super(message, cause);
        this.investorIndex = null;
        this.investorName = null;
        this.validationErrors = new ArrayList<>();
    }


    public InvestorValidationException(String message) {
        super(message);
        this.investorIndex = null;
        this.investorName = null;
        this.validationErrors = new ArrayList<>();
    }


    @Getter
    public static class ValidationError {
        private final String fieldName;
        private final Object rejectedValue;
        private final String errorMessage;
        private final String errorCode;

        private ValidationError(Builder builder) {
            this.fieldName = builder.fieldName;
            this.rejectedValue = builder.rejectedValue;
            this.errorMessage = builder.errorMessage;
            this.errorCode = builder.errorCode;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String fieldName;
            private Object rejectedValue;
            private String errorMessage;
            private String errorCode;

            public Builder fieldName(String fieldName) {
                this.fieldName = fieldName;
                return this;
            }

            public Builder rejectedValue(Object rejectedValue) {
                this.rejectedValue = rejectedValue;
                return this;
            }

            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }

            public Builder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public ValidationError build() {
                return new ValidationError(this);
            }
        }
    }


    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }


    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());

        if (investorName != null) {
            sb.append(" (Investor: ").append(investorName).append(")");
        }

        if (investorIndex != null) {
            sb.append(" (Index: ").append(investorIndex).append(")");
        }

        if (hasValidationErrors()) {
            sb.append("\nValidation errors:");
            for (ValidationError error : validationErrors) {
                sb.append("\n- ").append(error.getFieldName())
                        .append(": ").append(error.getErrorMessage());
                if (error.getRejectedValue() != null) {
                    sb.append(" (Rejected value: ").append(error.getRejectedValue()).append(")");
                }
            }
        }

        return sb.toString();
    }


    public static InvestorValidationException requiredField(String fieldName, Integer investorIndex, String investorName) {
        return new InvestorValidationException(
                String.format("Required field '%s' is missing or empty", fieldName),
                fieldName,
                null,
                investorIndex,
                investorName
        );
    }


    public static InvestorValidationException invalidEmail(String fieldName, String email,
                                                           Integer investorIndex, String investorName) {
        return new InvestorValidationException(
                String.format("Invalid email format for field '%s'", fieldName),
                fieldName,
                email,
                investorIndex,
                investorName
        );
    }


    public static InvestorValidationException valueTooLong(String fieldName, String value, int maxLength,
                                                           Integer investorIndex, String investorName) {
        return new InvestorValidationException(
                String.format("Value for field '%s' is too long (max %d characters)", fieldName, maxLength),
                fieldName,
                value,
                investorIndex,
                investorName
        );
    }


    public static InvestorValidationException duplicateName(String investorName, Integer investorIndex) {
        return new InvestorValidationException(
                String.format("Investor with name '%s' already exists", investorName),
                "name",
                investorName,
                investorIndex,
                investorName
        );
    }
}