package com.group34.eval_suite.ai.openai.config;

import com.group34.eval_suite.ai.openai.client.AqueductOpenAiClient;
import com.group34.eval_suite.ai.openai.client.OpenAiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class OpenAiClientConfiguration {

  @Bean
  OpenAiClient openAiClient(final OpenAiProperties properties) {
    return new AqueductOpenAiClient(properties);
  }
}
