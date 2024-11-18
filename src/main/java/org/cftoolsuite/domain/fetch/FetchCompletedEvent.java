package org.cftoolsuite.domain.fetch;

import java.util.List;

import org.springframework.context.ApplicationEvent;

public class FetchCompletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<FetchResult> results;

    public FetchCompletedEvent(Object source) {
        super(source);
    }

    public FetchCompletedEvent results(List<FetchResult> results) {
        this.results = results;
        return this;
    }

    public List<FetchResult> getResults() {
        return results;
    }

    public Object getSource() {
        return source;
    }
}
