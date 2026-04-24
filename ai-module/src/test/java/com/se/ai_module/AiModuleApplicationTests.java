package com.se.ai_module;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.base-url=https://api.deepseek.com",
		"spring.ai.openai.chat.options.model=deepseek-chat"
})
class AiModuleApplicationTests {

	@Test
	void contextLoads() {
	}

}
