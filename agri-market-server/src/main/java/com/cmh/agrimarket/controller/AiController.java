package com.cmh.agrimarket.controller;

import com.cmh.agrimarket.common.ApiResponse;
import com.cmh.agrimarket.dto.AiChatRequest;
import com.cmh.agrimarket.dto.AiChatResponse;
import com.cmh.agrimarket.service.AiCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiCustomerService aiCustomerService;

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest req) {
        return ApiResponse.ok(new AiChatResponse(aiCustomerService.chat(req.message(), req.history())));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStream(@Valid @RequestBody AiChatRequest req) {
        StreamingResponseBody body = outputStream -> {
            String reply = aiCustomerService.chat(req.message(), req.history());
            for (int i = 0; i < reply.length(); i++) {
                outputStream.write(String.valueOf(reply.charAt(i)).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                try {
                    Thread.sleep(18);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(body);
    }
}
