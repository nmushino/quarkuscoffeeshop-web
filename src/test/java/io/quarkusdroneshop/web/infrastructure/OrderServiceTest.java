package io.quarkusdroneshop.web.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkusdroneshop.domain.Item;
import io.quarkusdroneshop.domain.OrderLineItem;
import io.quarkusdroneshop.domain.OrderSource;
import io.quarkusdroneshop.web.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.web.infrastructure.testsupport.KafkaTestResource;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.ws.rs.sse.InboundSseEvent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest @QuarkusTestResource(KafkaTestResource.class)
public class OrderServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceTest.class);

    private static final String expectedPayload = "{\"id\":\"82124c69-a108-4ccc-9ac4-64566e389178\",\"orderSource\":\"WEB\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":null,\"QDCA10LineItems\":[{\"item\":\"QDC_A101\",\"price\":2.5,\"name\":\"Bugs\"}],\"QDCA10ProLineItems\":[]}";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    OrderService orderService;

    @Test
    public void testOrderServicerOrderIn() {

        InMemorySink<String> sink = connector.sink("orders-up");

        PlaceOrderCommand placeOrderCommand = new PlaceOrderCommand(
                "82124c69-a108-4ccc-9ac4-64566e389178",
                "ATLANTA",
                OrderSource.COUNTER,
                null,
                Arrays.asList(new OrderLineItem(Item.QDC_A101, BigDecimal.valueOf(135.50), "Bugs")),
                null,
                BigDecimal.valueOf(2.50)
        );

        orderService.placeOrder(placeOrderCommand);

        assertEquals(sink.received().size(), 1, "1 message should be sent");

        String receivedPayload = sink.received().get(0).getPayload();
        LOGGER.info("payload received: {}", receivedPayload);

        assertEquals(receivedPayload, expectedPayload, "payload should match");

    }

    // verify that the event matches our expectations
    private static Consumer<InboundSseEvent> onEvent = (inboundSseEvent) -> {
        String data = inboundSseEvent.readData();
        LOGGER.info("event received: {}", data);
        assertEquals(expectedPayload, data);
    };

}
