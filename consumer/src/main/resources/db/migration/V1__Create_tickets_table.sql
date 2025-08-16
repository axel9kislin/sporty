CREATE TABLE tickets (
    ticket_id UUID PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    assignee_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
