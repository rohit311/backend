Kafka start : /opt/homebrew/opt/kafka/bin/kafka-server-start /opt/homebrew/etc/kafka/server-1.properties

Create topic : kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 2 --partitions 4 --topic telusko

Check brokers running : kafka-topics —describe —topic telusko —bootstrap-server localhost:9092
