FROM bitnami/rabbitmq:3.7.11
USER root
COPY /bitnami /bitnami/

HEALTHCHECK --timeout=60s CMD curl --fail http://localhost:15672/ || exit 1