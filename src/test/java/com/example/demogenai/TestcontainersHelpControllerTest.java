package com.example.demogenai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ContainersConfiguration.class, IngestionConfiguration.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestcontainersHelpControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(TestcontainersHelpControllerTest.class);

	@Value("classpath:/validator-agent/system-prompt.st")
	private Resource systemPrompt;

	@Value("classpath:/validator-agent/user-prompt.st")
	private Resource userPrompt;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ChatModel chatModel;

	private ChatClient chatClient;

	@BeforeEach
	void setUp() {
		this.chatClient = ChatClient.builder(this.chatModel).defaultSystem(this.systemPrompt).build();
	}

	private final BeanOutputConverter<ValidatorAgentResponse> outputParser = new BeanOutputConverter<>(
			ValidatorAgentResponse.class);

	@Test
	void contextLoads() {
	}

	@Test
	void verifyWhichTestcontainersModulesAreAvailableInJava() {
		var question = "Which Testcontainers modules are available in Java?";
		var answer = restTemplate.getForObject("/help?message={question}", String.class, question);
		var reference = """
				- Answer must include a brief explanation of Testcontainers
				- Answer must include the available modules in Testcontainers
				- The modules must be: pgvector, ollama, mysql, redpanda
				- Answer must be less than 5 sentences
				""";

		evaluation(question, answer, reference, "yes");
	}

	@Test
	void verifyHowToUseTestcontainersOllamaInJava() {
		var question = "How can I use Testcontainers Ollama in Java?";
		var answer = restTemplate.getForObject("/help?message={question}", String.class, question);
		var reference = """
				- Answer must indicate to instantiate an Ollama Container by using OllamaContainer ollama = new OllamaContainer("ollama/ollama:0.1.26")
				- Answer must indicate to use org.testcontainers:ollama:1.19.7
				""";

		evaluation(question, answer, reference, "yes");
	}

	record ValidatorAgentResponse(String response, String reason) {
	}

	private void evaluation(String question, String answer, String reference, String expected) {
		String content = this.chatClient.prompt()
			.system(prompt -> prompt.param("format", this.outputParser.getFormat()))
			.user(user -> user.text(this.userPrompt)
				.params(Map.of("question", question, "answer", answer, "reference", reference)))
			.call()
			.content();
		ValidatorAgentResponse validation = this.outputParser.convert(content);
		logger.info("Validation: {}", validation);
		assertThat(validation.response()).isEqualTo(expected);
	}

}
