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
    system_prompt_id UUID NOT NULL REFERENCES system_prompts(id),
    system_prompt_family_id UUID NOT NULL,
    system_prompt_version_number INTEGER NOT NULL CHECK (system_prompt_version_number > 0),
    system_prompt_name VARCHAR(120) NOT NULL,
    system_prompt_content TEXT NOT NULL CHECK (char_length(system_prompt_content) BETWEEN 1 AND 5000),
    dataset_id UUID NOT NULL REFERENCES datasets(id),
    dataset_name VARCHAR(120) NOT NULL,
    dataset_item_count INTEGER NOT NULL CHECK (dataset_item_count > 0),
    status VARCHAR(20) NOT NULL,
    average_output_score NUMERIC(5,2) NULL CHECK (average_output_score BETWEEN 0 AND 100),
    average_prompt_score NUMERIC(5,2) NULL CHECK (average_prompt_score BETWEEN 0 AND 100),
    error_message TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    deleted_at TIMESTAMPTZ NULL
);

CREATE TABLE IF NOT EXISTS run_items (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    dataset_item_id UUID NOT NULL,
    position INTEGER NOT NULL CHECK (position >= 0),
    input TEXT NOT NULL CHECK (char_length(input) BETWEEN 1 AND 5000),
    expected_output TEXT NOT NULL CHECK (char_length(expected_output) BETWEEN 1 AND 5000),
    model_output TEXT NULL,
    output_score INTEGER NULL CHECK (output_score BETWEEN 0 AND 100),
    prompt_score INTEGER NULL CHECK (prompt_score BETWEEN 0 AND 100),
    judge_explanation TEXT NULL,
    improvement_suggestion TEXT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ NULL,
    CONSTRAINT uq_run_items_run_position UNIQUE (run_id, position)
);

CREATE INDEX IF NOT EXISTS idx_runs_active_created ON runs (created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_runs_status ON runs (status);
CREATE INDEX IF NOT EXISTS idx_run_items_run_position ON run_items (run_id, position);
