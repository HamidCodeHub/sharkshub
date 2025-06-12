package com.ucapital.sharkshub.investor.controller;

import com.ucapital.sharkshub.investor.dto.BulkOperationResponse;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import com.ucapital.sharkshub.investor.dto.OperationStatus;
import com.ucapital.sharkshub.investor.service.InvestorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/investors")
@Tag(name = "Investor API", description = "APIs for investor management and bulk operations")
public class InvestorController {

    private static final Logger logger = LoggerFactory.getLogger(InvestorController.class);

    private final InvestorService investorService;

    @Autowired
    public InvestorController(InvestorService investorService) {
        this.investorService = investorService;
    }


    @PostMapping("/bulk")
    @Operation(summary = "Bulk insert investors",
            description = "Insert multiple investors at once via JSON payload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investors processed",
                    content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Server error during processing")
    })
    public ResponseEntity<BulkOperationResponse> bulkInsert(
            @Parameter(description = "List of investors to insert")
            @Valid @RequestBody List<InvestorDto> investors) {

        logger.info("Received bulk insert request for {} investors", investors.size());
        BulkOperationResponse response = investorService.bulkInsert(investors);

        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/bulk/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk insert investors from file",
            description = "Insert multiple investors from a CSV or JSON file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed",
                    content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or format"),
            @ApiResponse(responseCode = "500", description = "Server error during processing")
    })
    public ResponseEntity<BulkOperationResponse> bulkInsertFromFile(
            @Parameter(description = "File containing investor data (CSV or JSON)")
            @RequestParam("file") MultipartFile file) {

        logger.info("Received bulk insert request from file: {}", file.getOriginalFilename());

        try {
            BulkOperationResponse response = investorService.bulkInsertFromFile(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage(), e);

            BulkOperationResponse errorResponse = BulkOperationResponse.builder()
                    .totalProcessed(1)
                    .successCount(0)
                    .failureCount(1)
                    .status(OperationStatus.FAILED)
                    .message("File processing failed: " + e.getMessage())
                    .build();

            errorResponse.addError(0, file.getOriginalFilename(), "FILE_READ_ERROR", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping(value = "/bulk/file/async", consumes = "multipart/form-data")
    @Operation(summary = "Launch async bulk insert job",
            description = "Starts a batch job to import investors from CSV or JSON, returns a jobExecutionId you can poll for status")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Job launched successfully",
                    content = @Content(schema = @Schema(type = "integer", example = "12345"))),
            @ApiResponse(responseCode = "400", description = "Invalid file or format"),
            @ApiResponse(responseCode = "500", description = "Server error during job launch")
    })
    public ResponseEntity<Long> launchBulkInsertJob(
            @RequestPart("file") MultipartFile file) {
        logger.info("Received async bulk insert request from file: {}", file.getOriginalFilename());
        try {
            long jobExecutionId = investorService.launchBulkInsertJob(file);
            return ResponseEntity.accepted().body(jobExecutionId);
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error launching job: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/bulk/file/status/{jobExecutionId}")
    @Operation(summary = "Get status of an async bulk insert job",
            description = "Poll for the current status and any validation errors/warnings of a previously-launched job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current job status",
                    content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "500", description = "Server error retrieving status")
    })
    public ResponseEntity<BulkOperationResponse> getBulkJobStatus(
            @PathVariable("jobExecutionId") long jobExecutionId) {
        logger.info("Fetching status for bulk insert job {}", jobExecutionId);
        try {
            BulkOperationResponse status = investorService.getBulkJobStatus(jobExecutionId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error fetching job status {}: {}", jobExecutionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping
    @Operation(summary = "Get all investors",
            description = "Retrieve all investors from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investors retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<InvestorDto>> getAllInvestors() {
        logger.debug("Received request to get all investors");
        List<InvestorDto> investors = investorService.findAll();
        return ResponseEntity.ok(investors);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get investor by ID",
            description = "Retrieve an investor by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investor found"),
            @ApiResponse(responseCode = "404", description = "Investor not found")
    })
    public ResponseEntity<InvestorDto> getInvestorById(
            @Parameter(description = "ID of the investor to retrieve")
            @PathVariable String id) {

        logger.debug("Received request to get investor with ID: {}", id);

        Optional<InvestorDto> investor = investorService.findById(id);
        return investor
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/by-name/{name}")
    @Operation(summary = "Get investor by name",
            description = "Retrieve an investor by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Investor found"),
            @ApiResponse(responseCode = "404", description = "Investor not found")
    })
    public ResponseEntity<InvestorDto> getInvestorByName(
            @Parameter(description = "Name of the investor to retrieve")
            @PathVariable String name) {

        logger.debug("Received request to get investor with name: {}", name);

        Optional<InvestorDto> investor = investorService.findByName(name);
        return investor
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}