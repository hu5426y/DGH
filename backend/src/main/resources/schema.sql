DROP TABLE IF EXISTS check_ins;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(32) NOT NULL UNIQUE,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(64) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_user_role (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS locations (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_location_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tickets (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  images TEXT NOT NULL,
  location_id VARCHAR(36) NOT NULL,
  reporter_id VARCHAR(36) NOT NULL,
  status VARCHAR(32) NOT NULL,
  assigned_to_name VARCHAR(64),
  assigned_to_contact VARCHAR(64),
  rating INT,
  feedback TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_ticket_status (status),
  INDEX idx_ticket_created (created_at),
  INDEX idx_ticket_reporter (reporter_id),
  INDEX idx_ticket_location (location_id),
  CONSTRAINT fk_ticket_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
  CONSTRAINT fk_ticket_location FOREIGN KEY (location_id) REFERENCES locations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS check_ins (
  id VARCHAR(36) PRIMARY KEY,
  ticket_id VARCHAR(36) NOT NULL,
  technician_name VARCHAR(64) NOT NULL,
  location VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_checkin_ticket (ticket_id),
  CONSTRAINT fk_checkins_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
