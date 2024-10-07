package com.example.demogenai;

import io.micrometer.core.annotation.Counted;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/help")
public class TestcontainersHelpController {

	private final ChatClient chatClient;

	private final VectorStore vectorStore;

	public TestcontainersHelpController(ChatModel chatModel, VectorStore vectorStore,
			@Value("classpath:/system-prompt.txt") Resource systemPrompt) {
		this.chatClient = ChatClient.builder(chatModel)
			.defaultSystem(systemPrompt)
			.defaultAdvisors(new SimpleLoggerAdvisor())
			.build();
		this.vectorStore = vectorStore;
	}

	@Counted
	@GetMapping
	public String help(@RequestParam(value = "message", defaultValue = "Help me with Testcontainers") String message) {
		return callResponseSpec(this.chatClient, this.vectorStore, message).content();
	}

	static ChatClient.CallResponseSpec callResponseSpec(ChatClient chatClient, VectorStore vectorStore,
			String question) {
		return chatClient.prompt().advisors(new QuestionAnswerAdvisor(vectorStore)).user(question).call();
	}

}
