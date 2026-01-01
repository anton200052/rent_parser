CREATE TABLE IF NOT EXISTS task_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    provider_type   TEXT NOT NULL,
    provider_url    TEXT NOT NULL,
    active          INTEGER NOT NULL DEFAULT 1,   -- BOOLEAN как 0/1
    telegram_token  TEXT NOT NULL,
    iterations      INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS telegram_chat_ids (
        task_info_id INTEGER NOT NULL,
        chat_id      TEXT NOT NULL,
        FOREIGN KEY (task_info_id) REFERENCES task_info(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS task_handled_listings (
        task_info_id INTEGER NOT NULL,
        listing_id   TEXT NOT NULL,
        PRIMARY KEY (task_info_id, listing_id),
        FOREIGN KEY (task_info_id) REFERENCES task_info(id) ON DELETE CASCADE,
        FOREIGN KEY (listing_id) REFERENCES handled_listings(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS handled_listings (
    id               TEXT PRIMARY KEY,
    price            TEXT NOT NULL,
    area_sq_meters   TEXT NOT NULL,
    rooms_value      TEXT NOT NULL,
    description      TEXT NOT NULL,
    location         TEXT NOT NULL,
    published        TEXT NOT NULL,
    link             TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS settings (
    id               INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id          INTEGER NOT NULL,
    work_from        TEXT NOT NULL,
    work_until       TEXT NOT NULL,
    interval_minutes INTEGER NOT NULL DEFAULT 60,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roles (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
     id       INTEGER PRIMARY KEY AUTOINCREMENT,
     username TEXT NOT NULL UNIQUE,
     password TEXT NOT NULL,
     enabled  INTEGER NOT NULL DEFAULT 1,
     role_id  INTEGER,
     last_scheduled_run TEXT,
     FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
);