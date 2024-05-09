package com.example.demogenai;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;

import java.util.List;
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
	private OllamaChatClient chatClient;

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
		var systemPrompt = new PromptTemplate(this.systemPrompt)
			.create(Map.of("format", this.outputParser.getFormat()));
		var systemMessage = new SystemMessage(systemPrompt.getContents());
		var promptTemplate = new PromptTemplate(this.userPrompt);
		var userMessage = promptTemplate
			.createMessage(Map.of("question", question, "answer", answer, "reference", reference));
		var prompt = new Prompt(List.of(systemMessage, userMessage));
		String content = this.chatClient.call(prompt).getResult().getOutput().getContent();
		ValidatorAgentResponse validation = this.outputParser.parse(content);
		logger.info("Validation: {}", validation);
		assertThat(validation.response()).isEqualTo(expected);
	}

}
