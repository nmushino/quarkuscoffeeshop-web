package io.quarkuscoffeeshop.web.infrastructure;


import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkuscoffeeshop.domain.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;

import static io.quarkuscoffeeshop.web.infrastructure.JsonUtil.toJson;

@RegisterForReflection
@ApplicationScoped
public class OrderService {

    Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Inject
    @Channel("orders-out")
    Emitter<String> ordersOutEmitter;

    public CompletableFuture<Void> placeOrder(final PlaceOrderCommand placeOrderCommand){
        return ordersOutEmitter.send(toJson(placeOrderCommand))
            .whenComplete((result, ex) -> {
                logger.debug("order sent: {}", placeOrderCommand);
                if (ex != null) {
                    logger.error(ex.getMessage());
                }
            }).toCompletableFuture();
    }
}
