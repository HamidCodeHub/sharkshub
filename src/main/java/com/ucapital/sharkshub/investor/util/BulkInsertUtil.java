package com.ucapital.sharkshub.investor.util;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.ucapital.sharkshub.investor.dto.BulkOperationResponse;
import com.ucapital.sharkshub.investor.dto.OperationStatus;
import com.ucapital.sharkshub.investor.model.Investor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class BulkInsertUtil {

    private static final Logger logger = LoggerFactory.getLogger(BulkInsertUtil.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final MongoTemplate mongoTemplate;

    public BulkInsertUtil(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public BulkOperationResponse bulkInsert(List<Investor> investors) {
        return bulkInsert(investors, DEFAULT_BATCH_SIZE);
    }


    public BulkOperationResponse bulkInsert(List<Investor> investors, int batchSize) {
        if (investors == null || investors.isEmpty()) {
            return BulkOperationResponse.builder()
                    .totalProcessed(0)
                    .successCount(0)
                    .failureCount(0)
                    .status(OperationStatus.COMPLETED)
                    .message("No investors to process")
                    .build();
        }

        logger.info("Starting bulk insert of {} investors with batch size {}", investors.size(), batchSize);

        Instant startTime = Instant.now();
        BulkOperationResponse response = BulkOperationResponse.builder()
                .totalProcessed(investors.size())
                .build();

        MongoCollection<Document> collection = mongoTemplate
                .getCollection(mongoTemplate.getCollectionName(Investor.class));

        int totalBatches = (investors.size() + batchSize - 1) / batchSize; // Ceiling division
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            int fromIndex = batchIndex * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, investors.size());
            List<Investor> batch = investors.subList(fromIndex, toIndex);

            try {
                int batchSuccess = processBatch(batch, collection, response, fromIndex);
                successCounter.addAndGet(batchSuccess);

                logger.debug("Processed batch {}/{}: {} investors, {} successful",
                        batchIndex + 1, totalBatches, batch.size(), batchSuccess);
            } catch (Exception e) {
                logger.error("Error processing batch {}/{}: {}", batchIndex + 1, totalBatches, e.getMessage(), e);
                for (int i = 0; i < batch.size(); i++) {
                    int itemIndex = fromIndex + i;
                    Investor investor = batch.get(i);

                    if (!response.getErrors().stream()
                            .anyMatch(error -> error.getItemIndex() == itemIndex)) {
                        response.addError(itemIndex, investor.getName(), "BATCH_PROCESSING_ERROR", e.getMessage());
                    }
                }
            }
        }

        response.setSuccessCount(successCounter.get());
        response.setFailureCount(response.getTotalProcessed() - response.getSuccessCount());

        Instant endTime = Instant.now();
        response.setDurationMs(Duration.between(startTime, endTime).toMillis());
        response.updateStatus();

        logger.info("Bulk insert completed in {}ms. Total: {}, Success: {}, Failed: {}",
                response.getDurationMs(), response.getTotalProcessed(),
                response.getSuccessCount(), response.getFailureCount());

        return response;
    }


    private int processBatch(List<Investor> batch, MongoCollection<Document> collection,
                             BulkOperationResponse response, int batchStartIndex) {
        int successCount = 0;

        try {
            List<WriteModel<Document>> writeModels = new ArrayList<>();
            List<Integer> indexMap = new ArrayList<>(); // Maps writeModels indices to original indices

            for (int i = 0; i < batch.size(); i++) {
                try {
                    int originalIndex = batchStartIndex + i;
                    Investor investor = batch.get(i);

                    Document document = new Document();
                    mongoTemplate.getConverter().write(investor, document);

                    writeModels.add(new InsertOneModel<>(document));
                    indexMap.add(originalIndex);
                } catch (Exception e) {
                    logger.error("Error converting investor at index {}: {}",
                            batchStartIndex + i, e.getMessage(), e);

                    Investor investor = batch.get(i);
                    response.addError(batchStartIndex + i, investor.getName(),
                            "CONVERSION_ERROR", e.getMessage());
                }
            }

            if (writeModels.isEmpty()) {
                return 0;
            }

            BulkWriteOptions options = new BulkWriteOptions().ordered(false); // Use unordered for better performance
            BulkWriteResult result = collection.bulkWrite(writeModels, options);

            successCount = result.getInsertedCount();


            if (successCount < writeModels.size()) {
                logger.warn("Partial success in batch: {} of {} succeeded",
                        successCount, writeModels.size());


                response.addWarning(String.format(
                        "Partial success detected in batch. %d of %d operations succeeded. " +
                                "Individual failures cannot be precisely identified.",
                        successCount, writeModels.size()));
            }
        } catch (Exception e) {
            logger.error("Failed to execute bulk write: {}", e.getMessage(), e);
            throw e;
        }

        return successCount;
    }


    public BulkOperationResponse bulkInsertWithSpringData(List<Investor> investors) {
        if (investors == null || investors.isEmpty()) {
            return BulkOperationResponse.builder()
                    .totalProcessed(0)
                    .successCount(0)
                    .failureCount(0)
                    .status(OperationStatus.COMPLETED)
                    .message("No investors to process")
                    .build();
        }

        logger.info("Starting bulk insert of {} investors using Spring Data", investors.size());

        Instant startTime = Instant.now();
        BulkOperationResponse response = BulkOperationResponse.builder()
                .totalProcessed(investors.size())
                .build();

        try {
            List<Investor> savedInvestors = mongoTemplate.insertAll(investors).stream().toList();
            response.setSuccessCount(savedInvestors.size());
            response.setFailureCount(investors.size() - savedInvestors.size());
        } catch (Exception e) {
            logger.error("Error during Spring Data bulk insert: {}", e.getMessage(), e);
            response.setSuccessCount(0);
            response.setFailureCount(investors.size());
            response.addError(0, null, "BULK_INSERT_ERROR", e.getMessage());
        }

        Instant endTime = Instant.now();
        response.setDurationMs(Duration.between(startTime, endTime).toMillis());
        response.updateStatus();

        logger.info("Spring Data bulk insert completed in {}ms. Total: {}, Success: {}, Failed: {}",
                response.getDurationMs(), response.getTotalProcessed(),
                response.getSuccessCount(), response.getFailureCount());

        return response;
    }
}