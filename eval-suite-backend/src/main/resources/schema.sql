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
