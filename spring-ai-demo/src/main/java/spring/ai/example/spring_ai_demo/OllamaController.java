package spring.ai.example.spring_ai_demo;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class OllamaController {

    private final ChatClient chatClient;

    public OllamaController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping
    public String chat(@RequestParam String message) {
        return chatClient.prompt().user(message).call().content();
    }
}

