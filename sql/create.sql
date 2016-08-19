CREATE TABLE person (
    id integer primary key,
    name varchar(100) NOT NULL UNIQUE,
    age integer NOT NULL
);