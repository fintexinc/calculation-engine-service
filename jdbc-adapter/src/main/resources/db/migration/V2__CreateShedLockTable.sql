create table if not exists shedlock
(
    name       VARCHAR,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR,
    PRIMARY KEY (name)
)