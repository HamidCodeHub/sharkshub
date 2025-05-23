# sharkshub
Bulk investor insertion service
# Progetto Tecnico: Servizio di Inserimento Massivo di Investor

Questo repository contiene la soluzione al piccolo progetto tecnico proposto da [Nome Azienda] a Hamid, con l’obiettivo di valutare il suo approccio allo sviluppo con Spring Boot e le sue competenze in un contesto pratico.

## Obiettivo

Implementare una **Spring Boot** application che:

- Esponga endpoint REST per l’inserimento **bulk** di entità **Investor**
- Supporti input via **JSON** (API) e **upload di file** (CSV/JSON)
- Utilizzi **MongoDB** come database
- Garantica **inserimenti bulk** efficienti
- Includa **validazione**, **gestione degli errori**, **transaction management** e **logging**
- Preveda **unit** e **integration test**

---

## Caratteristiche principali

- **Bulk Insert via JSON**  
  `POST /api/investors/bulk`
    - Body: `List<InvestorDto>`
    - Risposta: `BulkOperationResponse` con conteggi di successi, errori e dettagli.

- **Bulk Insert via File**  
  `POST /api/investors/bulk/file`
    - `multipart/form-data` con file `.csv` o `.json`
    - Parsing automatico e validazione.

- **CRUD Standard**
    - `GET /api/investors`
    - `GET /api/investors/{id}`
    - `GET /api/investors/by-name/{name}`

- **Validazione**
    - Bean Validation (Jakarta) + validatore custom (`InvestorValidator`)
    - Fail-fast per performance bulk

- **Gestione errori**
    - `GlobalExceptionHandler` centralizzato
    - Risposte strutturate con `ApiError`

- **Configurazione MongoDB**
    - Pool di connessioni ottimizzato
    - `WriteConcern.MAJORITY` per durabilità

- **Logging e Monitoring**
    - Logback configurato in `logback-spring.xml`
    - Actuator (opzionale)

- **Testing**
    - Unit test per servizi e validatori
    - Integration test con MongoDB embedded

---

## Struttura del progetto


---

## Prerequisiti

- Java 21
- Maven 3.9+
- MongoDB in esecuzione su `localhost:27017`
- (Opzionale) MongoDB Compass per ispezionare i dati

---

## Installazione e Avvio

1. **Clona il repository**
   ```bash
   git clone https://github.com/HamidCodeHub/sharkshub.git
   cd sharkshub
   

Costruisci e avvia

mvn clean install
mvn spring-boot:run
L’applicazione sarà disponibile su http://localhost:8080/swagger-ui/index.html 


TEST

mvn clean test

mvn -Dtest=InvestorServicePerformanceTest test
