package com.example.demogenai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
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

	@Value("classpath:/user-prompt.st")
	private Resource userPrompt;

	public TestcontainersHelpController(ChatModel chatModel, VectorStore vectorStore,
			@Value("classpath:/system-prompt.txt") Resource systemPrompt) {
		this.chatClient = ChatClient.builder(chatModel).defaultSystem(systemPrompt).build();
		this.vectorStore = vectorStore;
	}

	@GetMapping
	public String help(@RequestParam(value = "message", defaultValue = "Help me with Testcontainers") String message) {
		return this.chatClient.prompt()
			.user(prompt -> prompt.text(this.userPrompt).param("question", message))
			.advisors(new QuestionAnswerAdvisor(this.vectorStore))
			.call()
			.content();
	}

}
