-- remove old redundant column (if existed from legacy)
ALTER TABLE metadata
DROP COLUMN IF EXISTS actors;

-- ============================
-- ACTOR table
-- ============================
CREATE TABLE actor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id INT NOT NULL,
    character VARCHAR(255),
    profile_path VARCHAR(500)
);

CREATE UNIQUE INDEX idx_actor_actor_id ON actor (actor_id);

-- ============================
-- MAPPING table
-- ============================
CREATE TABLE metadata_actor_mapping (
    metadata_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    PRIMARY KEY (metadata_id, actor_id),

    CONSTRAINT fk_metadata_actor_metadata
        FOREIGN KEY (metadata_id)
            REFERENCES metadata(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_metadata_actor_actor
        FOREIGN KEY (actor_id)
            REFERENCES actor(id)
            ON DELETE CASCADE
);

-- ============================
-- Indexes
-- ============================
CREATE INDEX idx_metadata_tmdb_id ON metadata (tmdb_id);
CREATE INDEX idx_metadata_title ON metadata (title);
CREATE INDEX idx_actor_name ON actor (character);