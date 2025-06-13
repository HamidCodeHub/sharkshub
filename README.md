# SharksHub - Bulk Investor Insertion Service

A high-performance Spring Boot application designed for efficient bulk insertion and management of investor data, featuring both synchronous and asynchronous processing capabilities.

## 🚀 Overview

SharksHub provides a comprehensive solution for handling large-scale investor data operations with:
- **Synchronous bulk operations** for immediate processing
- **Asynchronous batch processing** for high-volume file imports
- **Multi-format support** (JSON, CSV)
- **Advanced validation** and error handling
- **Duplicate detection** and prevention
- **RESTful API** with OpenAPI documentation

## 🏗️ Architecture

### Technology Stack
- **Java 21** - Latest LTS version
- **Spring Boot 3.4.5** - Main framework
- **Spring Batch** - Asynchronous processing engine
- **MongoDB** - Primary data storage
- **H2 Database** - Spring Batch metadata store
- **Bean Validation (Jakarta)** - Data validation
- **OpenAPI 3** - API documentation
- **Maven** - Build management

### Key Components
- **REST Controllers** - API endpoints
- **Service Layer** - Business logic
- **Batch Processing** - Async file processing
- **Validation Engine** - Custom validation rules
- **File Parsers** - Multi-format support
- **Error Handling** - Comprehensive exception management

## 📊 Data Model

### Investor Entity
```
Investor
├── Basic Info (name, status, type, macroType)
├── Contact Details (website, emails)
├── Geographic & Investment Preferences
├── HQ Location (Address)
├── Financial Metrics (Financials)
├── Multi-language Descriptions
└── Contact Persons (Set<Contacts>)
```

## 🔧 API Endpoints

### Bulk Operations

#### Synchronous Bulk Insert
```http
POST /api/investors/bulk
Content-Type: application/json

[
  {
    "name": "Venture Capital Fund",
    "status": "ACTIVE",
    "type": "VC",
    "website": "https://example.com"
  }
]
```

#### File Upload (Synchronous)
```http
POST /api/investors/bulk/file
Content-Type: multipart/form-data

file: investors.csv | investors.json
```

#### Asynchronous File Processing
```http
POST /api/investors/bulk/file/async
Content-Type: multipart/form-data

file: large_investors.csv
```
**Response**: `202 Accepted` with job execution ID
```json
{
  "jobExecutionId": 12345
}
```

#### Job Status Monitoring
```http
GET /api/investors/bulk/file/status/{jobExecutionId}
```
**Response**:
```json
{
  "totalProcessed": 10000,
  "successCount": 9950,
  "failureCount": 50,
  "status": "COMPLETED",
  "message": "Job completed successfully",
  "errors": [],
  "warnings": []
}
```

### Standard CRUD Operations

```http
GET    /api/investors              # Get all investors (paginated)
GET    /api/investors/{id}         # Get investor by ID
GET    /api/investors/by-name/{name} # Get investor by name
DELETE /api/investors/{id}         # Delete investor
```

## ⚡ Asynchronous Processing Features

### Spring Batch Integration
- **Custom ItemReader**: Handles both CSV and JSON formats
- **Chunk Processing**: Configurable batch sizes (default: 100)
- **Fault Tolerance**: Skip invalid records, continue processing
- **Duplicate Detection**: Prevents duplicate entries during batch processing
- **Progress Tracking**: Real-time job execution monitoring

### File Processing Workflow
1. **File Upload** → Temporary storage with checksum calculation
2. **Duplicate Check** → Prevents reprocessing of identical files
3. **Job Launch** → Spring Batch job creation with unique parameters
4. **Background Processing** → Async chunk-based processing
5. **Status Monitoring** → Real-time progress tracking via REST API

### Async Job Status Types
- `IN_PROGRESS` - Job is currently running
- `COMPLETED` - Job finished successfully
- `PARTIAL_SUCCESS` - Some records failed, others succeeded
- `FAILED` - Job failed completely

### Performance Optimizations
- **Unordered Bulk Writes** - MongoDB bulk operations
- **Connection Pooling** - Optimized database connections
- **Chunk-based Processing** - Memory-efficient processing
- **Skip Logic** - Continue processing despite individual failures

## 📁 Supported File Formats

### CSV Format
```csv
name,status,type,macroType,website,creatorEmail
"Acme Ventures","ACTIVE","VC","VENTURE_CAPITAL","https://acme.com","creator@acme.com"
```

### JSON Format
```json
[
  {
    "name": "Acme Ventures",
    "status": "ACTIVE",
    "type": "VC",
    "macroType": "VENTURE_CAPITAL",
    "website": "https://acme.com",
    "hqLocation": {
      "city": "San Francisco",
      "country": "USA"
    },
    "financials": {
      "invMin": 100000,
      "invMax": 10000000
    }
  }
]
```

## 🛡️ Validation & Error Handling

### Built-in Validations
- **Required Fields**: name, status, type
- **Email Format**: All email fields
- **URL Format**: Website URLs
- **Length Limits**: Configurable field lengths
- **Uniqueness**: Investor name uniqueness
- **Nested Object Validation**: Address, contacts, financials

### Error Response Structure
```json
{
  "status": "BAD_REQUEST",
  "message": "Validation failed",
  "timestamp": "2025-01-01T10:00:00Z",
  "details": {
    "investorName": "Invalid Investor",
    "validationErrors": [
      {
        "field": "name",
        "message": "Name is required",
        "rejectedValue": null
      }
    ]
  }
}
```

## 🚦 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- MongoDB 4.4+ running on `localhost:27017`
- (Optional) MongoDB Compass for data inspection

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/HamidCodeHub/sharkshub.git
   cd sharkshub
   ```

2. **Configure MongoDB** (optional)
   ```properties
   # application.properties
   spring.data.mongodb.host=localhost
   spring.data.mongodb.port=27017
   spring.data.mongodb.database=sharkshub
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
  - **API Documentation**: http://localhost:8080/swagger-ui/index.html
  - **Health Check**: http://localhost:8080/actuator/health
  - **Application**: http://localhost:8080

## 🧪 Testing

### Run All Tests
```bash
mvn clean test
```

### Performance Tests
```bash
mvn -Dtest=InvestorServicePerformanceTest test
```

### Test Coverage
- **Unit Tests**: Service layer, validation, utilities
- **Integration Tests**: MongoDB integration, batch processing
- **Performance Tests**: Bulk operations benchmarking

## 📈 Performance Metrics

### Benchmarks (10,000 records)
- **Synchronous Bulk Insert**: < 15 seconds
- **File Processing (CSV)**: < 30 seconds
- **Single Record Retrieval**: < 500ms
- **Paginated Queries**: < 2 seconds

### Scalability Features
- **Configurable Batch Sizes**: Tune for your hardware
- **Connection Pooling**: Efficient resource utilization
- **Chunk Processing**: Memory-efficient for large files
- **Skip Logic**: Fault-tolerant processing

## 🔧 Configuration

### MongoDB Settings
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=sharkshub
spring.data.mongodb.create-unique-index=true
```

### Batch Processing Settings
```properties
spring.batch.job.enabled=false  # Manual job triggering
spring.batch.jdbc.initialize-schema=always
```

### File Upload Limits
```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

## 🔍 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **MongoDB Connection**: Automatic health indicators
- **Batch Job Status**: Real-time job monitoring

### Logging
- **Structured Logging**: JSON format with correlation IDs
- **Performance Metrics**: Execution time tracking
- **Error Tracking**: Comprehensive error logging
- **Audit Trail**: Data change tracking

## 🛠️ Development

### Project Structure
```
src/main/java/com/ucapital/sharkshub/
├── config/                 # Configuration classes
├── investor/
│   ├── controller/         # REST controllers
│   ├── dto/               # Data transfer objects
│   ├── model/             # JPA entities
│   ├── repository/        # Data repositories
│   ├── service/           # Business logic
│   ├── util/              # Utility classes
│   ├── validation/        # Custom validators
│   └── exception/         # Exception handling
└── SharkshubApplication.java
```

### Adding New Features
1. **New Endpoints**: Add to `InvestorController`
2. **Validation Rules**: Extend `InvestorValidator`
3. **Batch Processing**: Modify `InvestorBatchConfig`
4. **File Formats**: Extend `FileParser`

## 🐛 Troubleshooting

### Common Issues

#### MongoDB Connection Issues
```bash
# Check MongoDB status
systemctl status mongod

# Start MongoDB
systemctl start mongod
```

#### Memory Issues (Large Files)
- Increase JVM heap size: `-Xmx4g`
- Reduce batch chunk size in configuration
- Use async processing for large files

#### Batch Job Failures
- Check job execution logs
- Verify file format and content
- Monitor database connection pool

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is proprietary software owned by Ucapital.

## 📞 Support

For technical support or questions:
- **Email**: info@ucapital.com
- **Documentation**: Check Swagger UI at `/swagger-ui/index.html`
- **Issues**: Submit via GitHub issues

---

**SharksHub** - Powering efficient investor data management with cutting-edge technology.