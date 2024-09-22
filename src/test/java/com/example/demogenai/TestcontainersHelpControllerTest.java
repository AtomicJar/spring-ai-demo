package com.example.demogenai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Content;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ContainersConfiguration.class, IngestionConfiguration.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "logging.level.org.springframework.ai.chat.client.advisor=DEBUG" })
class TestcontainersHelpControllerTest {

	static final String BESPOKE_MINICHECK = "bespoke-minicheck:7b";

	@Value("classpath:/validator-agent/system-prompt.st")
	private Resource systemPrompt;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private OllamaApi ollamaApi;

	@Autowired
	private VectorStore vectorStore;

	private ChatClient chatClient;

	@BeforeEach
	void setUp() {
		ChatModel chatModel = new OllamaChatModel(ollamaApi,
				OllamaOptions.builder().withModel(BESPOKE_MINICHECK).withNumPredict(2).withTemperature(0.0f).build());
		this.chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void verifyWhichTestcontainersModulesAreAvailableInJava() {
		var question = "Which Testcontainers modules are available in Java?";
		var answer = restTemplate.getForObject("/help?message={question}", String.class, question);

		var content = chatResponse(question);
		evaluation(answer, content, "Yes");
	}

	@Test
	void verifyHowToUseTestcontainersOllamaInJava() {
		var question = "How can I use Testcontainers Ollama in Java?";
		var answer = restTemplate.getForObject("/help?message={question}", String.class, question);

		var content = chatResponse(question);

		evaluation(answer, content, "Yes");
	}

	private String chatResponse(String question) {
		var response = this.chatClient.prompt()
			.advisors(new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.defaults()))
			.user(question)
			.call()
			.chatResponse();
		return ((List<Content>) response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS))
				.stream()
				.map(Content::getContent)
				.filter(Objects::nonNull)
				.filter(c -> c instanceof String)
				.map(Objects::toString)
				.collect(Collectors.joining(System.lineSeparator()));
	}

	private void evaluation(String answer, String reference, String expected) {
		String content = this.chatClient.prompt()
			.user(prompt -> prompt.text(this.systemPrompt).params(Map.of("document", reference, "claim", answer)))
			.call()
			.content();
		assertThat(content).isEqualTo(expected);
	}

}
