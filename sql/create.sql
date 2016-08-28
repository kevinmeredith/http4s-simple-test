CREATE TABLE person (
    ssn NUMERIC(8) primary key,
    name VARCHAR(100) NOT NULL UNIQUE,
    age integer NOT NULL
);