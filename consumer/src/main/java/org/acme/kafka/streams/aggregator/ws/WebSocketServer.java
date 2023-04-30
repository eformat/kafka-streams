package org.acme.kafka.streams.aggregator.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.acme.kafka.streams.aggregator.model.Aggregation;
import org.acme.kafka.streams.aggregator.model.AggregationEncoder;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/temps/{clientId}", encoders = AggregationEncoder.class)
@ApplicationScoped
public class WebSocketServer {

    private static final Logger log = Logger.getLogger(WebSocketServer.class);

    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        sessions.put(clientId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("clientId") String clientId) {
        sessions.remove(clientId);
    }

    @OnError
    public void onError(Session session, @PathParam("clientId") String clientId, Throwable throwable) {
        sessions.remove(clientId);
    }

    @Incoming("temperatures-aggregated")
    public CompletionStage<Void> handleData(Message<Aggregation> data) {
        log.info(">> " + data.getPayload());
        broadcast(data.getPayload());
        return data.ack();
    }

    public void broadcast(Aggregation message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    log.error("Unable to send message: " + result.getException());
                }
            });
        });
    }

}
