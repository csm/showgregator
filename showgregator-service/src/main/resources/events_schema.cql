CREATE TABLE showgregator.events (
    calendar uuid,
    id uuid,
    when timestamp,
    title text,
    venue uuid,
    tags map<ascii, text>,
    PRIMARY KEY (calendar, id, when)
) WITH CLUSTERING ORDER BY (id DESC);