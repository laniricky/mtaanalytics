CREATE TABLE IF NOT EXISTS weekly_uploads (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    week_start_date VARCHAR(10) NOT NULL,
    youtube_uploads INTEGER NOT NULL DEFAULT 0,
    tiktok_uploads INTEGER NOT NULL DEFAULT 0,
    facebook_uploads INTEGER NOT NULL DEFAULT 0,
    instagram_uploads INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, week_start_date)
);

CREATE TABLE IF NOT EXISTS custom_goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_value DOUBLE PRECISION NOT NULL,
    current_value DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    deadline TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);
