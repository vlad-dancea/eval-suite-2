package com.group34.eval_suite.ai.openai.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eval-suite.open-ai")
public record OpenAiProperties(URI baseUrl, String apiKey, String modelName) {}
