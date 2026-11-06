package workshop.notificationservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import workshop.notificationservice.dao.NotificationEntity
import workshop.notificationservice.dto.NotificationEvent
import workshop.notificationservice.dto.TicketEvent
import workshop.notificationservice.producer.NotificationEventProducer
import workshop.notificationservice.repository.NotificationRepository
import workshop.notificationservice.utils.NOTIFICATION_CREATED

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val notificationEventProducer: NotificationEventProducer
) : NotificationService {
    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)

    override fun processTicketCreatedEvent(ticketEvent: TicketEvent) {
        createAndSendNotification(ticketEvent, "CREATED")
    }

    override fun processTicketUpdatedEvent(ticketEvent: TicketEvent) {
        createAndSendNotification(ticketEvent, ticketEvent.status)
    }

    private fun createAndSendNotification(ticketEvent: TicketEvent, messageStatus: String) {
        val notification = NotificationEntity(
            ticketId = ticketEvent.ticketId,
            customerId = ticketEvent.customerId,
            message = createNotificationMessage(ticketEvent.ticketId, messageStatus),
            status = ticketEvent.status,
            timestamp = System.currentTimeMillis()
        )
        val savedNotification = notificationRepository.save(notification)

        sendNotificationToCustomer(savedNotification)

        val notificationId = requireNotNull(savedNotification.id) {
            "Saved notification did not have an ID"
        }
        notificationEventProducer.produce(
            NotificationEvent(
                eventType = NOTIFICATION_CREATED,
                notificationId = notificationId,
                ticketId = savedNotification.ticketId,
                customerId = savedNotification.customerId,
                message = savedNotification.message,
                status = savedNotification.status,
                timestamp = savedNotification.timestamp
            )
        )
    }

    private fun createNotificationMessage(ticketId: String, status: String): String {
        return when (status.uppercase()) {
            "CREATED" ->
                "Your support ticket #$ticketId has been created and is being processed. We'll keep you updated on progress."

            "IN_PROGRESS" ->
                "Good news! Your ticket #$ticketId is now being actively worked on by our team."

            "RESOLVED" ->
                "Your ticket #$ticketId has been resolved. Please contact us if you need further assistance."

            "CLOSED" ->
                "Ticket #$ticketId has been closed. Thank you for contacting support!"

            else -> "Your ticket #$ticketId status has been updated to: $status"
        }
    }

    /**
     * Mock implementation of sending a notification to a customer. In a real implementation,
     * this could send emails, SMS, push notifications, etc.
     */
    private fun sendNotificationToCustomer(notificationDao: NotificationEntity) {
        logger.info(
            "[SIMULATION] Sending notification to customer {}: {}",
            notificationDao.customerId,
            notificationDao.message
        )
    }
}
