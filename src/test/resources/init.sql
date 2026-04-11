-- Legacy-compatible schema - DO NOT MODIFY (R3)
-- Table: users

CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(200) NOT NULL,
  login VARCHAR(20) NOT NULL,
  password VARCHAR(100) NOT NULL,
  PRIMARY KEY (id)
);

-- -----------------
-- Phase 2: CIS
-- -----------------

CREATE TABLE IF NOT EXISTS topics (
    id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_users FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ideas(
    id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    content TEXT NOT NULL,
    topic_id VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ideas_topics FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_ideas_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS votes(
    id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    idea_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_votes_ideas FOREIGN KEY (idea_id) REFERENCES ideas(id),
    CONSTRAINT fk_votes_users FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (idea_id, user_id)
);
