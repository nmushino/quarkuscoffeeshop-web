package io.quarkusdroneshop.web.infrastructure;


import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.web.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.web.domain.commands.WebOrderCommand;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;

import static io.quarkusdroneshop.web.infrastructure.JsonUtil.toJson;

@RegisterForReflection
@ApplicationScoped
public class OrderService {

    Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Inject
    @Channel("orders-up")
    Emitter<String> ordersOutEmitter;

    public CompletableFuture<Void> placeOrder(final PlaceOrderCommand placeOrderCommand){

        logger.debug("PlaceOrderCommandReceived: {}", placeOrderCommand);

        WebOrderCommand webOrderCommand = new WebOrderCommand(placeOrderCommand);

        logger.debug("WebOrderCommand: {}", webOrderCommand);

        return ordersOutEmitter.send(toJson(webOrderCommand))
            .whenComplete((result, ex) -> {
                logger.debug("order sent: {}", placeOrderCommand);
                if (ex != null) {
                    logger.error(ex.getMessage());
                }
            }).toCompletableFuture();
    }
}
