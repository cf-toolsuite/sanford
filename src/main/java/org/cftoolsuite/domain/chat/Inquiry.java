package org.cftoolsuite.domain.chat;

import java.util.List;

public record Inquiry(String question, List<FilterMetadata> filter) {
}
