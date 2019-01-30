FROM bitnami/rabbitmq:latest
USER root
COPY /bitnami /bitnami/

HEALTHCHECK --timeout=60s CMD curl --fail http://localhost:15672/ || exit 1