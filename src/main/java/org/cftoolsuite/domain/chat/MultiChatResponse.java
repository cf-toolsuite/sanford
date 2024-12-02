package org.cftoolsuite.domain.chat;

public record MultiChatResponse(
        String model,
        String response,
        String error
) {
    // Optional constructor to simplify successful response creation
    public static MultiChatResponse success(String model, String response) {
        return new MultiChatResponse(model, response, null);
    }

    // Optional constructor to simplify error response creation
    public static MultiChatResponse failure(String model, String error) {
        return new MultiChatResponse(model, null, error);
    }
}