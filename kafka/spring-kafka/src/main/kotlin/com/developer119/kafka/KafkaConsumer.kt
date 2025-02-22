package com.developer119.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer {

    @KafkaListener(topics = ["test-topic"], groupId = "my-kafka-group")
    fun listen(record: ConsumerRecord<String, String>) {
        println("Received message: ${record.value()} from topic: ${record.topic()}")
    }
}
