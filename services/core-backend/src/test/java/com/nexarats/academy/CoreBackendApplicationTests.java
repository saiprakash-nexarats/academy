package com.nexarats.academy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class CoreBackendApplicationTests {

	@Test
	@DisplayName("CoreBackendApplication context loads cleanly with configured services")
	void contextLoads() {
	}

}
