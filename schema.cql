CREATE TABLE events (
    id uuid,
    when timestamp,
    title text,
    venue uuid,
    link text,
    info text,
    acl map<text, int>,
    PRIMARY KEY (id, when)
) WITH CLUSTERING ORDER BY (when DESC);

CREATE TABLE calendars (
    id uuid,
    title text,
    acl map<text, int>,
    PRIMARY KEY (id)
);

CREATE TABLE events_in_calendar (
    calendar uuid,
    event uuid,
    when timestamp,
    title text,
    PRIMARY KEY (calendar, event, when)
) WITH CLUSTERING ORDER BY (when DESC);

CREATE TYPE address (
    street text,
    city text,
    state text,
    country text
);

CREATE TYPE geolocation (
    latitude double,
    longitude double
);

CREATE TABLE venue (
    id uuid PRIMARY KEY,
    name text,
    location geolocation,
    address address
);

CREATE TYPE hashed_password (
    hash_alg ascii,
    salt blob,
    iterations int,
    hash blob
);

CREATE TABLE users (
    email text PRIMARY KEY,
    handle text,
    password hashed_password
);