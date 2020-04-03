restful-clojure
===============

TODO:
Update ragtime
Use hugsql instead of korma

## Setup

### Dev SQL

CREATE ROLE restful_dev WITH LOGIN PASSWORD 'pass_dev';
CREATE DATABASE restful_dev WITH OWNER restful_dev;

### Dev Cmd-line

lein ragtime migrate

### Test SQL

CREATE ROLE restful_test WITH LOGIN PASSWORD 'pass_test';
CREATE DATABASE restful_test WITH OWNER restful_test;

### Test Cmd-line

lein with-profile test ragtime migrate

## Running

To start a web server for the application, run:

    lein ring server [port]
