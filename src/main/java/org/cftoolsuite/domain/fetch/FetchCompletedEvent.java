package org.cftoolsuite.domain.fetch;

import org.springframework.context.ApplicationEvent;

import java.util.List;

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
