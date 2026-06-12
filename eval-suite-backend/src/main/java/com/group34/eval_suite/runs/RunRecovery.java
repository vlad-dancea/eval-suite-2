package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunItem;
import com.group34.eval_suite.runs.enums.RunStatus;
import com.group34.eval_suite.runs.repo.RunRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("PMD.LawOfDemeter")
public class RunRecovery implements ApplicationRunner {

  private static final String STOPPED_MSG =
      "Run did not finish before the backend stopped. Start a new run.";

  private final RunRepository runRepository;

  public RunRecovery(RunRepository runRepository) {
    this.runRepository = runRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    final List<Run> interrupted =
        runRepository.findByStatusInAndDeletedAtIsNull(
            List.of(RunStatus.QUEUED, RunStatus.RUNNING));
    final OffsetDateTime now = OffsetDateTime.now();

    for (final Run run : interrupted) {
      run.setStatus(RunStatus.FAILED);
      run.setErrorMessage(STOPPED_MSG);
      run.setCompletedAt(now);

      for (final RunItem item : run.getItems()) {
        if (item.getStatus() == RunStatus.QUEUED || item.getStatus() == RunStatus.RUNNING) {
          item.setStatus(RunStatus.FAILED);
          item.setErrorMessage(STOPPED_MSG);
          item.setCompletedAt(now);
        }
      }
    }
  }
}
