package com.perkss.kafka.reactive

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderRecord

@SpringBootApplication
class KafkaReactiveApp : CommandLineRunner {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaReactiveApp::class.java)
    }

    override fun run(vararg args: String) {

        logger.info("Running Kafka Reactive App: Uppercase Topology")

        val bootstrapServers = "localhost:9092"
        val topic = "lowercase-topic"
        val outputTopic = "uppercase-topic"

        val producer = KafkaReactiveProducer(bootstrapServers)

        val consumer = KafkaReactiveConsumer(bootstrapServers, topic)

        consumer.consume()
                .map {
                    logger.info("Received message: {}", it)
                    it.receiverOffset().acknowledge() // TODO is both required.
                    it.receiverOffset().commit()
                    it
                }
                .map {
                    val producerRecord = ProducerRecord(outputTopic, it.key(), it.value().toUpperCase())
                    SenderRecord.create(producerRecord, it.key())
                }
                .map { producer.send(Mono.just(it)) }
                .doOnError { logger.error("An error has occurred {}", it) }
                .subscribe {
                    logger.info("Subscribing to Consumer and Producer")
                }
    }

}

fun main(args : Array<String>) {
    runApplication<KafkaReactiveApp>(*args)
}


