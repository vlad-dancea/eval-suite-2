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
