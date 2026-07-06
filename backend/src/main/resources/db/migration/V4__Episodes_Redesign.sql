DROP TABLE IF EXISTS episodes CASCADE;

CREATE TABLE IF NOT EXISTS episodes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    published_at BIGINT NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS episode_links (
    id UUID PRIMARY KEY,
    episode_id UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    url TEXT,
    view_count BIGINT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL,
    UNIQUE(episode_id, platform)
);
