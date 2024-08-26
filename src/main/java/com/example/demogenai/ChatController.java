package com.example.demogenai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

	private final ChatClient chatClient;

	public ChatController(ChatModel chatModel) {
		this.chatClient = ChatClient.builder(chatModel).build();
	}

	@GetMapping("/generate")
	public String generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return this.chatClient.prompt().user(message).call().content();
	}

	@GetMapping("/prompt")
	public String generatePrompts(@RequestParam(value = "subject", defaultValue = "software engineer") String subject) {
		var promptTemplate = new PromptTemplate("Tell me a {subject} joke");
		var prompt = promptTemplate.create(Map.of("subject", subject));
		return this.chatClient.prompt(prompt).call().content();
	}

	@GetMapping("/jokes")
	public String jokes() {
		var systemMessage = new SystemMessage("Your main job is to tell dad jokes");
		var userMessage = new UserMessage("Tell me a joke");
		return this.chatClient.prompt().messages(systemMessage, userMessage).call().content();
	}

}
