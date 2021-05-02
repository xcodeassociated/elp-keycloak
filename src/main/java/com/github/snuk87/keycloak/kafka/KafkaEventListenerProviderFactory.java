package com.github.snuk87.keycloak.kafka;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final Logger LOG = Logger.getLogger(KafkaEventListenerProviderFactory.class);
	private static final String ID = "kafka";

	private KafkaEventListenerProvider instance;

	private String bootstrapServers;
	private String topicEvents;
	private String topicAdminEvents;
	private String clientId;
	private String[] events;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			instance = new KafkaEventListenerProvider(bootstrapServers, clientId, topicEvents, events,
					topicAdminEvents);
		}

		return instance;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(Scope config) {
		LOG.info(">>> Init kafka module ...");
		topicEvents = System.getenv("KEYCLOAK_KAFKA_TOPIC_EVENTS");//config.get("topicEvents");
		topicAdminEvents = System.getenv("KEYCLOAK_KAFKA_TOPIC_ADMIN"); //config.get("topicAdminEvents");
		clientId = System.getenv("KEYCLOAK_KAFKA_CLIENTID"); //config.get("clientId", "keycloak");
		bootstrapServers = System.getenv("KEYCLOAK_KAFKA_BOOTSTRAPSERVER"); //config.get("bootstrapServers");
		LOG.info(">>> Init kafka module >> env read successes!");

		String eventsString = config.get("events");

		if (eventsString != null) {
			events = eventsString.split(",");
		}

		if (topicEvents == null) {
			throw new NullPointerException("topic must not be null.");
		}

		if (clientId == null) {
			throw new NullPointerException("clientId must not be null.");
		}

		if (bootstrapServers == null) {
			throw new NullPointerException("bootstrapServers must not be null");
		}

		if (events == null || events.length == 0) {
			events = new String[3];
			events[0] = "REGISTER";
			events[1] = "LOGIN";
		  events[2] = "LOGOUT";
		}
		LOG.info(">>> Init kafka module finished");
	}

	@Override
	public void postInit(KeycloakSessionFactory arg0) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
