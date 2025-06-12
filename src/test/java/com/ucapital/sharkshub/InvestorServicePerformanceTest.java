package com.ucapital.sharkshub;

import com.ucapital.sharkshub.investor.repository.InvestorRepository;
import com.ucapital.sharkshub.investor.service.InvestorService;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        properties = "spring.batch.job.repository-type=map"
)
@ActiveProfiles("test")
class InvestorServicePerformanceTest {

    @Autowired
    private InvestorService investorService;

    @Autowired
    private InvestorRepository investorRepository;

    private List<InvestorDto> largeDtoList;
    private MockMultipartFile largeCsvFile;

    @BeforeAll
    void setupData() throws IOException {
        largeDtoList = IntStream.rangeClosed(1, 10_000)
                .mapToObj(InvestorServicePerformanceTest::createDummyDto)
                .collect(Collectors.toList());

        String header = "id,name,status,type,macroType,website\n";
        StringBuilder sb = new StringBuilder(header);
        for (InvestorDto dto : largeDtoList) {
            sb.append(dto.getId()).append(",")
                    .append(dto.getName()).append(",")
                    .append(dto.getStatus()).append(",")
                    .append(dto.getType()).append(",")
                    .append(dto.getMacroType()).append(",")
                    .append(dto.getWebsite()).append("\n");
        }
        largeCsvFile = new MockMultipartFile(
                "file",
                "investors.csv",
                "text/csv",
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))
        );
    }

    @BeforeEach
    void cleanDbAndWarmUp() {
        investorRepository.deleteAll();
        investorService.bulkInsert(largeDtoList.subList(0, 100));
    }

    @AfterAll
    void cleanDb() {
        investorRepository.deleteAll();
    }

    @Test
    void bulkInsertShouldCompleteWithin15Seconds() {
        assertTimeoutPreemptively(Duration.ofSeconds(15),
                () -> investorService.bulkInsert(largeDtoList),
                "bulkInsert di 10k elementi deve completare in meno di 15 secondi");
    }

    @Test
    void bulkInsertFromFileShouldCompleteWithin30Seconds() {
        assertTimeoutPreemptively(Duration.ofSeconds(30),
                () -> investorService.bulkInsertFromFile(largeCsvFile),
                "bulkInsertFromFile su CSV da 10k righe deve completare in meno di 30 secondi");
    }

    @Test
    void findAllShouldCompleteWithin2Seconds() {
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> {
                    var all = investorService.findAll();
                    if (CollectionUtils.isEmpty(all)) {
                        throw new IllegalStateException("Nessun investitore trovato");
                    }
                },
                "findAll() deve completare in meno di 2 secondi");
    }

    @Test
    void findByIdShouldCompleteWithin500Millis() {
        String testId = largeDtoList.get(0).getId();
        assertTimeoutPreemptively(Duration.ofMillis(500),
                () -> investorService.findById(testId)
                        .orElseThrow(() -> new IllegalStateException("Investor non trovato")),
                "findById() deve completare in meno di 500 ms");
    }

    private static InvestorDto createDummyDto(int i) {
        InvestorDto dto = new InvestorDto();
        dto.setId("inv-" + i);
        dto.setName("Investor " + i);
        dto.setStatus("ACTIVE");
        dto.setType("TYPE" + (i % 5));
        dto.setMacroType("MACRO" + (i % 3));
        dto.setWebsite("https://investor" + i + ".example.com");
        return dto;
    }
}