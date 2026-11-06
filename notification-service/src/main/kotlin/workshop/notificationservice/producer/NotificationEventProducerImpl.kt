package workshop.notificationservice.producer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import workshop.notificationservice.dto.NotificationEvent
import workshop.notificationservice.utils.NOTIFICATION_TOPIC

@Component
class NotificationEventProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : NotificationEventProducer {
    private val logger = LoggerFactory.getLogger(NotificationEventProducerImpl::class.java)
    private val objectMapper = jacksonObjectMapper()

    override fun produce(notificationEvent: NotificationEvent) {
        try {
            val json = objectMapper.writeValueAsString(notificationEvent)
            logger.info("Producing notification event to topic '{}': {}", NOTIFICATION_TOPIC, json)
            kafkaTemplate.send(NOTIFICATION_TOPIC, notificationEvent.notificationId, json)
        } catch (e: Exception) {
            logger.error("Error producing notification event: {}", notificationEvent, e)
        }
    }
}