-- ================================================
-- V1__create_table_metadata.sql
-- Create table for Metadata entity
-- ================================================

CREATE TABLE IF NOT EXISTS metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    movie_id BIGINT NOT NULL,
    retrieved BOOLEAN,
    search_title VARCHAR(255),
    tmdb_id INT,
    for_adult BOOLEAN,
    title VARCHAR(255),
    original_title VARCHAR(255),
    description VARCHAR(2000),
    number_of_episodes INT,
    vote_average DOUBLE,
    vote_count INT,
    popularity DOUBLE,
    poster_path VARCHAR(500),
    backdrop_path VARCHAR(500),
    release_date DATE,
    country VARCHAR(255),
    original_language VARCHAR(50),
    genre TEXT,
    actors TEXT,
    status VARCHAR(100),

    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NULL,

    CONSTRAINT uq_metadata_movie_id UNIQUE (movie_id),
    CONSTRAINT uq_metadata_tmdb_id UNIQUE (tmdb_id)
);
