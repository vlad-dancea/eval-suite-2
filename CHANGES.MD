# Changes After Poster Session Feedback

## Feedback Received

> "There seems to be a lack of agentic work going on. It looks like a basic 'do something and see what comes out' and then change it yourself kind of application. There needs to be an agentic worker on improving the prompts depending on past results."

## What We Changed

We added an **automatic prompt improvement loop** that turns the application from a manual "run-and-read" tool into an agentic system that iterates on prompts autonomously.

### Automatic Improvement Pipeline (`RunExecutionService`)

When a user enables the "Automatic prompt improvement" toggle on the Create Run modal, the system now:

1. Runs the initial evaluation (generate model outputs, grade each output, grade the system prompt).
2. Sends the current prompt and all per-item results to an LLM-based improver agent that rewrites the system prompt to better match expected outputs.
3. Creates a new system prompt version and a child run, then evaluates the improved prompt against the same dataset.
4. Compares the new prompt grade to the previous best:
   - **If the score improves:** keeps the new version as the best and loops again (up to 10 iterations).
   - **If the score does not improve:** discards the new prompt version and stops the loop.
5. All intermediate runs and prompt versions are persisted, so users can inspect the full improvement timeline in the Run Detail modal.

### Poster Layout Reordering

We also received feedback that the poster should read top to bottom. In response, we swapped the content of page 1 and page 2 so that the most important information (previously on page 2) now appears first, making the poster flow naturally from top to bottom.