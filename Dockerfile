FROM openjdk:17-alpine

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443
ENV LAS2PEER_PORT=9011

#update required software modules
RUN apk add --update bash dos2unix && rm -f /var/cache/apk/*

#create new system user group and add new user to it
RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer
	
#copy as no root user, copies current host directory into docker directory /src
COPY --chown=las2peer:las2peer . /src

WORKDIR /src

# run the rest as unprivileged user
USER las2peer
RUN dos2unix ./gradlew
RUN dos2unix /src/main.sh

RUN chmod +x ./gradlew
RUN chmod +x /src/main.sh
RUN ./main.sh -m build

EXPOSE $HTTP_PORT
EXPOSE $HTTPS_PORT
EXPOSE $LAS2PEER_PORT
CMD ["/src/main.sh", "-r", "start_one_node"]
