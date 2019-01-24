FROM bitnami/rabbitmq:latest
USER root
COPY /bitnami /bitnami/

HEALTHCHECK --interval=5s CMD curl --fail http://localhost:5671/ || exit 1