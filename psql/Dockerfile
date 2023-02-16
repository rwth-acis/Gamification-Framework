FROM postgres:alpine3.17

ENV PSQL_PORT=5432

RUN apk add --update bash dos2unix && rm -f /var/cache/apk/*

#ADD pg_hba.conf /var/lib/postgresql/data/
#ADD postgresql.conf /var/lib/postgresql/data/

COPY init.sql /docker-entrypoint-initdb.d/
RUN dos2unix /docker-entrypoint-initdb.d/init.sql

EXPOSE $PSQL_PORT
