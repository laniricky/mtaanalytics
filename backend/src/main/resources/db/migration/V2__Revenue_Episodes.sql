CREATE TABLE IF NOT EXISTS revenue_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    month_year VARCHAR(7) NOT NULL,
    youtube_revenue DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    tiktok_revenue DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    facebook_revenue DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    instagram_revenue DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sponsors DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    merchandise DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    website_income DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    other_income DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, month_year)
);

CREATE TABLE IF NOT EXISTS episodes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    season INTEGER NOT NULL,
    episode INTEGER NOT NULL,
    release_date TIMESTAMP NOT NULL,
    views BIGINT NOT NULL DEFAULT 0,
    revenue DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    watch_time_hours DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    shares BIGINT NOT NULL DEFAULT 0,
    comments BIGINT NOT NULL DEFAULT 0,
    likes BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, season, episode)
);
