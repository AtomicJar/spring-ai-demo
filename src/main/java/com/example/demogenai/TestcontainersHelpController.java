package com.example.demogenai;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/help")
public class TestcontainersHelpController {

	private final OllamaChatClient chatClient;

	private final VectorStore vectorStore;

	@Value("classpath:/system-prompt.txt")
	private Resource systemPrompt;

	@Value("classpath:/user-prompt.st")
	private Resource userPrompt;

	public TestcontainersHelpController(OllamaChatClient chatClient, VectorStore vectorStore) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
	}

	@GetMapping
	public String help(@RequestParam(value = "message", defaultValue = "Help me with Testcontainers") String message) {
		var docs = this.vectorStore.similaritySearch(SearchRequest.query(message))
			.stream()
			.map(Document::getContent)
			.collect(Collectors.joining(System.lineSeparator()));

		var systemMessage = new SystemMessage(this.systemPrompt);
		var promptTemplate = new PromptTemplate(this.userPrompt);
		var userMessage = promptTemplate.createMessage(Map.of("question", message, "documents", docs));

		var prompt = new Prompt(List.of(systemMessage, userMessage));

		return this.chatClient.call(prompt).getResult().getOutput().getContent();
	}

}
