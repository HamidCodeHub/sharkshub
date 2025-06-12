package com.ucapital.sharkshub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = "spring.batch.job.repository-type=map"
)
class SharkshubApplicationTests {

	@Test
	void contextLoads() {
	}

}
