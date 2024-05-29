package com.example.demogenai;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

	private final OllamaChatModel chatModel;

	public ChatController(OllamaChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@GetMapping("/generate")
	public String generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return this.chatModel.call(message);
	}

	@GetMapping("/prompt")
	public String generatePrompts(@RequestParam(value = "subject", defaultValue = "software engineer") String subject) {
		var promptTemplate = new PromptTemplate("Tell me a {subject} joke");
		var prompt = promptTemplate.create(Map.of("subject", subject));
		return this.chatModel.call(prompt).getResult().getOutput().getContent();
	}

	@GetMapping("/jokes")
	public String jokes() {
		var systemMessage = new SystemMessage("Your main job is to tell dad jokes");
		var userMessage = new UserMessage("Tell me a joke");
		var prompts = new Prompt(List.of(systemMessage, userMessage));
		return this.chatModel.call(prompts).getResult().getOutput().getContent();
	}

}
