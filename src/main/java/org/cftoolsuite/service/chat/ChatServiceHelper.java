package org.cftoolsuite.service.chat;

import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.Collection;
import java.util.Map;

class ChatServiceHelper {

    static VectorStoreDocumentRetriever.Builder constructDocumentRetriever(VectorStore vectorStore, Map<String, Object> filterMetadata) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op filterExpression = null;

        if (MapUtils.isNotEmpty(filterMetadata)) {
            for (Map.Entry<String, Object> entry : filterMetadata.entrySet()) {
                FilterExpressionBuilder.Op currentCondition;

                if (entry.getValue() instanceof Collection) {
                    currentCondition = b.in(entry.getKey(), (Collection<?>) entry.getValue());
                } else {
                    currentCondition = b.eq(entry.getKey(), entry.getValue());
                }

                if (filterExpression == null) {
                    filterExpression = currentCondition;
                } else {
                    filterExpression = b.and(filterExpression, currentCondition);
                }
            }
        }

        VectorStoreDocumentRetriever.Builder vsdrb = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStore);
        if (filterExpression != null) {
            vsdrb.filterExpression(filterExpression.build());
        }
        return vsdrb;
    }

}
