package com.ucapital.sharkshub.investor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponse {


    private int totalProcessed;


    private int successCount;


    private int failureCount;


    @Builder.Default
    private Instant timestamp = Instant.now();


    private long durationMs;


    @Builder.Default
    private OperationStatus status = OperationStatus.COMPLETED;

    private String message;

    @Builder.Default
    private List<BulkError> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    public boolean isFullySuccessful() {
        return failureCount == 0 && successCount == totalProcessed;
    }

    public boolean hasFailures() {
        return failureCount > 0;
    }

    public double getSuccessRate() {
        if (totalProcessed == 0) {
            return 0.0;
        }
        return (double) successCount / totalProcessed;
    }

    public void addError(int itemIndex, String investorName, String errorCode, String errorMessage) {
        this.errors.add(BulkError.builder()
                .itemIndex(itemIndex)
                .investorName(investorName)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build());
        this.failureCount++;
    }

    public void addValidationError(int itemIndex, String investorName, String fieldName,
                                   Object rejectedValue, String errorMessage) {
        this.errors.add(BulkError.builder()
                .itemIndex(itemIndex)
                .investorName(investorName)
                .errorCode("VALIDATION_ERROR")
                .errorMessage(errorMessage)
                .fieldName(fieldName)
                .rejectedValue(rejectedValue)
                .build());
        this.failureCount++;
    }


    public void addWarning(String warning) {
        this.warnings.add(warning);
    }


    public void updateStatus() {
        if (failureCount == 0) {
            this.status = OperationStatus.COMPLETED;
            this.message = String.format("Successfully processed all %d investors", successCount);
        } else if (successCount > 0) {
            this.status = OperationStatus.PARTIAL_SUCCESS;
            this.message = String.format("Processed %d of %d investors successfully. %d failed.",
                    successCount, totalProcessed, failureCount);
        } else {
            this.status = OperationStatus.FAILED;
            this.message = String.format("Failed to process all %d investors", totalProcessed);
        }
    }
}