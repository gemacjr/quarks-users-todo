-- Sample data for development and testing
-- This file will be executed automatically when the application starts

-- Insert sample users
INSERT INTO users (id, username, email, name, created_at, updated_at) VALUES
(1, 'john_doe', 'john.doe@example.com', 'John Doe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'jane_smith', 'jane.smith@example.com', 'Jane Smith', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'bob_wilson', 'bob.wilson@example.com', 'Bob Wilson', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample todos
INSERT INTO todos (id, title, description, completed, user_id, due_date, created_at, updated_at) VALUES
(1, 'Complete project documentation', 'Write comprehensive documentation for the API', false, 1, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Review pull requests', 'Review and approve pending pull requests', false, 1, CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Deploy to production', 'Deploy the latest version to production environment', false, 1, CURRENT_TIMESTAMP + INTERVAL '14 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Setup CI/CD pipeline', 'Configure automated testing and deployment', true, 1, CURRENT_TIMESTAMP - INTERVAL '1 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Write unit tests', 'Add comprehensive unit tests for all endpoints', false, 2, CURRENT_TIMESTAMP + INTERVAL '5 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Update dependencies', 'Update all project dependencies to latest versions', true, 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Fix security vulnerabilities', 'Address security issues reported in scan', false, 2, CURRENT_TIMESTAMP + INTERVAL '1 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Optimize database queries', 'Improve query performance for large datasets', false, 3, CURRENT_TIMESTAMP + INTERVAL '10 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Setup monitoring', 'Configure application monitoring and alerting', false, 3, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Code refactoring', 'Refactor legacy code to improve maintainability', true, 3, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Reset sequences to continue from the last inserted ID
ALTER SEQUENCE user_seq RESTART WITH 4;
ALTER SEQUENCE todo_seq RESTART WITH 11;
