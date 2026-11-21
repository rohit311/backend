package com.example.multimodal;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController 
@RequestMapping("/api/agent")
public class AgentController {
    private final MultimodalAgentService agentService;

    public AgentController(MultimodalAgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReceiptSummary runAgent(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", defaultValue = "Read this receipt and log the expense.") String prompt) throws IOException {
        return agentService.analyzeAndAct(file, prompt);
    }
}
