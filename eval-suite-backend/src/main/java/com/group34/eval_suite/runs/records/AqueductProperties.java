package com.group34.eval_suite.runs.records;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "aqueduct")
public record AqueductProperties(String mainModel, String judgeModel) {

  /** Falls back to the main model when no judge model is configured (unset or blank). */
  @Override
  public String judgeModel() {
    return StringUtils.hasText(judgeModel) ? judgeModel : mainModel;
  }
}
