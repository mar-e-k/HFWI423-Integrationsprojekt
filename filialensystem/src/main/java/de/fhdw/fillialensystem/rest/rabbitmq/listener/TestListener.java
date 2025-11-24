package de.fhdw.fillialensystem.rest.rabbitmq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener {

    public TestListener() {
        super();
    }

    @RabbitListener(queues = "test.queue")
    public void handle() {} //change later; only for demonstrative purposes
}