package com.example.demogenai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.FactCheckingEvaluator;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ContainersConfiguration.class, IngestionConfiguration.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "logging.level.org.springframework.ai.chat.client.advisor=DEBUG" })
class TestcontainersHelpControllerTest {

	static final String BESPOKE_MINICHECK = "bespoke-minicheck:7b";

	@LocalServerPort
	private int port;

	@Autowired
	private OllamaApi ollamaApi;

	@Autowired
	private VectorStore vectorStore;

	@Autowired
	private ChatClient.Builder chatClientBuilder;

	private ChatClient.Builder factCheckChatClientBuilder;

	@BeforeEach
	void setUp() {
		ChatModel chatModel = OllamaChatModel.builder()
			.ollamaApi(this.ollamaApi)
			.defaultOptions(OllamaOptions.builder().model(BESPOKE_MINICHECK).numPredict(2).temperature(0.0d).build())
			.build();
		this.factCheckChatClientBuilder = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor());
	}

	@Test
	void contextLoads() {
	}

	@Test
	void verifyWhichTestcontainersModulesAreAvailableInJava() {
		var question = "Which Testcontainers modules are available in Java?";
		var answer = retrieveAnswer(question);

		assertFactCheck(question, answer);
	}

	@Test
	void verifyHowToUseTestcontainersOllamaInJava() {
		var question = "How can I use Testcontainers Ollama in Java?";
		var answer = retrieveAnswer(question);

		assertFactCheck(question, answer);
	}

	private String retrieveAnswer(String question) {
		RestClient restClient = RestClient.builder().baseUrl("http://localhost:%d".formatted(this.port)).build();
		return restClient.get().uri("/help?message={question}", question).retrieve().body(String.class);
	}

	private void assertFactCheck(String question, String answer) {
		FactCheckingEvaluator factCheckingEvaluator = FactCheckingEvaluator
			.forBespokeMinicheck(this.factCheckChatClientBuilder);
		EvaluationResponse evaluate = factCheckingEvaluator.evaluate(new EvaluationRequest(docs(question), answer));
		assertThat(evaluate.isPass()).isTrue();
	}

	private List<Document> docs(String question) {
		var response = TestcontainersHelpController
			.callResponseSpec(this.chatClientBuilder.build(), this.vectorStore, question)
			.chatResponse();
		return response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
	}

}
