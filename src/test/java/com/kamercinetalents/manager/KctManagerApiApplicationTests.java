package com.kamercinetalents.manager;

import com.kamercinetalents.manager.integration.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
class KctManagerApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
