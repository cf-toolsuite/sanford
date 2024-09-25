package org.cftoolsuite.domain;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Map<String, String> supportedContentTypes) {}
