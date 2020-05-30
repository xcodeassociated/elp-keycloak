package com.github.snuk87.keycloak.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListenerProvider implements EventListenerProvider {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProvider.class);

	private String topicEvents;

	private List<EventType> events;

	private String topicAdminEvents;

	private Producer<String, String> producer;

	private ObjectMapper mapper;

	public KafkaEventListenerProvider(String bootstrapServers, String clientId, String topicEvents, String[] events,
			String topicAdminEvents) {
				LOG.info(">>> KafkaEventListenerProvider created");
		this.topicEvents = topicEvents;
		this.events = new ArrayList<>();
		this.topicAdminEvents = topicAdminEvents;

		for (int i = 0; i < events.length; i++) {
			try {
				LOG.info(">>> Adding event handler for: " + events[i]);
				EventType eventType = EventType.valueOf(events[i].toUpperCase());
				this.events.add(eventType);
			} catch (IllegalArgumentException e) {
				LOG.error("Ignoring event >" + events[i] + "<. Event does not exist.");
			}
		}

		producer = KafkaProducerFactory.createProducer(clientId, bootstrapServers);
		mapper = new ObjectMapper();
	}

	private void produceEvent(String eventAsString, String topic) throws InterruptedException, ExecutionException {
		LOG.info(">>> Produce to topic: " + topicEvents + " ...");
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventAsString);
		Future<RecordMetadata> metaData = producer.send(record);
		RecordMetadata recordMetadata = metaData.get();
		LOG.info(">>> Produced to topic: " + recordMetadata.topic());
	}

	@Override
	public void onEvent(Event event) {
		LOG.info(">>> onEvent: " + event.toString());
		// if (events.contains(event.getType())) {
		if (topicEvents != null) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicEvents);
			} catch (JsonProcessingException | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		} else {
			LOG.info(">>> topicEvents == null");
		}
		// } else {
		// 	LOG.info(">>> event type no match: " + event.getType().toString());
		// }
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		LOG.info(">>> onEvent Admin: " + event.toString());
		if (topicAdminEvents != null) {
			try {
				produceEvent(mapper.writeValueAsString(event), topicAdminEvents);
			} catch (JsonProcessingException | ExecutionException e) {
				LOG.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		} else {
			LOG.info(">>> topicAdminEvents == null");
		}
	}

	@Override
	public void close() {
		// ignore
	}
}
