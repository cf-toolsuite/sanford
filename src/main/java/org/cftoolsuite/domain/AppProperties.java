package org.cftoolsuite.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Map<String, String> supportedContentTypes) {}
