package org.cftoolsuite.domain.chat;

import java.util.Base64;

public record AudioResponse(String text, String audioBase64) {
    public AudioResponse(String text, byte[] audio) {
        this(text, Base64.getEncoder().encodeToString(audio));
    }
}
