package org.cftoolsuite.service.chat;

import org.apache.commons.collections4.CollectionUtils;
import org.cftoolsuite.domain.chat.FilterMetadata;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.Collection;
import java.util.List;

class ChatServiceHelper {

    static VectorStoreDocumentRetriever.Builder constructDocumentRetriever(VectorStore vectorStore, List<FilterMetadata> filterMetadata) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op filterExpression = null;

        if (CollectionUtils.isNotEmpty(filterMetadata)) {
            for (FilterMetadata entry : filterMetadata) {
                FilterExpressionBuilder.Op currentCondition;

                if (entry.value() instanceof Collection) {
                    currentCondition = b.in(entry.key(), (Collection<?>) entry.value());
                } else {
                    currentCondition = b.eq(entry.key(), entry.value());
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
