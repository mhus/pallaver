FROM eclipse-temurin:21.0.5_11-jdk

ENV UID=1001

RUN apt-get update && apt-get install -y unzip ca-certificates sudo \
    && useradd -u $UID -m user

RUN mkdir -p /app

COPY launcher/container/ /app/
COPY target/pallaver.jar /app/pallaver.jar
RUN chown -R $UID /app/config
EXPOSE 8080

USER $UID
ENTRYPOINT ["/app/run.sh"]
