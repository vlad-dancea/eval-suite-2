# Eval Suite

A lightweight testing platform to version LLM prompts and automatically grades their outputs.

## Initial Situation

One of the most challenging parts of building applications around LLMs is reliably measuring the quality of the output. Because LLMs are non-deterministic and there is no single correct answer most of the time, you cannot just write a simple unit test. There is no direct predictor about how different prompts, models parameters change a system’s end result. Most teams answer this by reading a few examples and subjectively judging them.. Tools like Braintrust or Humanloop exist to solve this; They let you version prompts, run them against test datasets, score the outputs, and compare configurations.

## Project Goal

In this project you will build your own lightweight version of such a platform. While this project leans towards being an engineering project more than a core Generative AI project it helps you get a better feeling of working with LLMs in production scenarios and teaches you how to design evaluation for non-deterministic systems. The focus is not on building a beautiful web dashboard, but on engineering the backend logic that reliably grades LLM outputs.

## High Level Concept

This system should treat prompts and execution parameters (model, mode-parameters, etc.) as versionable artefacts rather than raw strings in a python file. Think of git for prompts.

Users should be able to craft test cases: inputs paired with some notion of what a good output looks like. This can be an expected answer, a set of criteria, or simply the input alone (when scoring is done by a judge model). The system should then be able to take a prompt configuration (prompt text, model, parameters) and run it against a dataset.

LLM outputs should be scored using different methods which may include: LLM-as-judge (a second model rates the output), programmatic checks (regex, JSON schema validation, keyword presence), semantic similarity to a reference answer, or human ratings. One should be able to check the same prompts against different configurations and show a clear comparison.

## Learning goals / Core challenges
The core challenge is designing robust, repeatable evaluation metrics for non-deterministic text. You will need to engineer prompt templates for judging models and research and implement other metrics to score LLM outputs.

Take a look at Braintrust, Humanloop and Langsmith for inspiration.
