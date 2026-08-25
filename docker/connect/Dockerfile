FROM confluentinc/cp-kafka-connect:8.3.1

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.9.6 \
    && python3 -c "from urllib.request import urlretrieve; import tarfile; archive = '/tmp/opensearch-connector.tar'; urlretrieve('https://github.com/Aiven-Open/opensearch-connector-for-apache-kafka/releases/download/v3.2.0/opensearch-connector-for-apache-kafka-3.2.0.tar', archive); tarfile.open(archive).extractall('/usr/share/confluent-hub-components')" \
    && python3 -c "from urllib.request import urlretrieve; urlretrieve('https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar', '/usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib/mysql-connector-j-9.6.0.jar')"
