FROM registry.datapwn.com/vizotenko/components-integration-test-rabbitmq:1.0.0

COPY rabbitmq.config /bitnami/rabbitmq/conf/
COPY ca_certificate.pem /bitnami/rabbitmq/certificates/
COPY server_certificate.pem /bitnami/rabbitmq/certificates/
COPY server_key.pem /bitnami/rabbitmq/certificates/

#HEALTHCHECK --interval=5s --retries=10 CMD curl --fail http://localhost:8983/ || exit 1