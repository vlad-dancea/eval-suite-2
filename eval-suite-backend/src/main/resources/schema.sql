CREATE TABLE IF NOT EXISTS system_prompts (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    version_number INTEGER NOT NULL CHECK (version_number > 0),
    name VARCHAR(120) NOT NULL,
    content TEXT NOT NULL CHECK (char_length(content) BETWEEN 1 AND 5000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT uq_system_prompts_family_version UNIQUE (family_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_system_prompts_active_created ON system_prompts (created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_system_prompts_family_version_desc ON system_prompts (family_id, version_number DESC);

CREATE TABLE IF NOT EXISTS datasets (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE IF NOT EXISTS dataset_items (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL REFERENCES datasets(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position >= 0),
    input TEXT NOT NULL CHECK (char_length(input) BETWEEN 1 AND 5000),
    expected_output TEXT NOT NULL CHECK (char_length(expected_output) BETWEEN 1 AND 5000),
    CONSTRAINT uq_dataset_items_dataset_position UNIQUE (dataset_id, position)
);

CREATE INDEX IF NOT EXISTS idx_datasets_active_created ON datasets (created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dataset_items_dataset ON dataset_items (dataset_id);

CREATE TABLE IF NOT EXISTS runs (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    deleted_at TIMESTAMPTZ NULL,
    status VARCHAR(20) NOT NULL,
    system_prompt_id UUID NOT NULL REFERENCES system_prompts(id),
    dataset_id UUID NOT NULL REFERENCES datasets(id),
    output_score INTEGER NULL CHECK (output_score BETWEEN 0 AND 100),
    system_prompt_score INTEGER NULL CHECK (system_prompt_score BETWEEN 0 AND 100),
    system_prompt_feedback TEXT NULL,
    system_prompt_improvement_suggestion TEXT NULL,
    automatic_improvement_enabled BOOLEAN NOT NULL DEFAULT false,
    automatic_improvement_attempt INTEGER NULL CHECK (automatic_improvement_attempt >= 0),
    failure_message TEXT NULL
);

CREATE TABLE IF NOT EXISTS run_item_results (
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    dataset_item_id UUID NOT NULL REFERENCES dataset_items(id),
    position INTEGER NOT NULL CHECK (position >= 0),
    model_output TEXT NULL,
    output_score INTEGER NULL CHECK (output_score BETWEEN 0 AND 100),
    judge_feedback TEXT NULL,
    CONSTRAINT uq_run_item_results_run_position UNIQUE (run_id, position),
    CONSTRAINT uq_run_item_results_run_dataset_item UNIQUE (run_id, dataset_item_id)
);

CREATE INDEX IF NOT EXISTS idx_runs_created ON runs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_runs_system_prompt ON runs (system_prompt_id);
CREATE INDEX IF NOT EXISTS idx_runs_dataset ON runs (dataset_id);
CREATE INDEX IF NOT EXISTS idx_run_item_results_run ON run_item_results (run_id);
