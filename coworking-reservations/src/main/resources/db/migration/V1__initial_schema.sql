CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE,
                       created_by VARCHAR(255),
                       updated_at TIMESTAMP WITH TIME ZONE,
                       updated_by VARCHAR(255),
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE spaces (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(150) NOT NULL,
                        type VARCHAR(30) NOT NULL,
                        capacity INTEGER NOT NULL,
                        location VARCHAR(255) NOT NULL,
                        hourly_rate NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE,
                        created_by VARCHAR(255),
                        updated_at TIMESTAMP WITH TIME ZONE,
                        updated_by VARCHAR(255),
                        CONSTRAINT uk_spaces_name UNIQUE (name)
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              space_id BIGINT NOT NULL,
                              start_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                              end_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              payment_method VARCHAR(30) NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE,
                              created_by VARCHAR(255),
                              updated_at TIMESTAMP WITH TIME ZONE,
                              updated_by VARCHAR(255),

                              CONSTRAINT fk_reservations_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users (id),

                              CONSTRAINT fk_reservations_space
                                  FOREIGN KEY (space_id)
                                      REFERENCES spaces (id)
);

CREATE INDEX idx_reservations_space_dates
    ON reservations (space_id, start_date_time, end_date_time);

CREATE INDEX idx_reservations_user
    ON reservations (user_id);