# NOTE: When updating base image, be sure to update the label in docker-build.yml for base image
FROM ghcr.io/retrodaredevil/lavender-jre:21-ubuntu-noble
ARG JAR_LOCATION
ARG COMMIT_HASH=""
ARG REF=""


RUN ["mkdir", "/led-machine"]
COPY --chmod=0444 ${JAR_LOCATION} /led-machine/led-machine-plus.jar
ENV DOCKER=""
ENV COMMIT_HASH="$COMMIT_HASH"
ENV REF="$REF"

RUN ["mkdir", "/app"]
WORKDIR "/app"
ENTRYPOINT ["java", "-XX:ErrorFile=/app/jvmlog/hs_err_pid%p.log", "-jar", "/led-machine/led-machine-plus.jar"]
