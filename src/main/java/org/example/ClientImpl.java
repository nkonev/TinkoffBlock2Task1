package org.example;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientImpl implements Client {
    private final AtomicInteger count1 = new AtomicInteger(0);

    private final AtomicInteger count2 = new AtomicInteger(0);

    @Override
    public Response getApplicationStatus1(String id) {
        if (count1.get() == 0) {
            count1.set(1);
            return new Response.RetryAfter(Duration.ofSeconds(4));
        }

        try {
            TimeUnit.SECONDS.sleep(6);
            return new Response.Success("as1", id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response.Failure(e);
        }
    }

    @Override
    public Response getApplicationStatus2(String id) {
        if (count2.get() == 0) {
            count2.set(1);
            return new Response.RetryAfter(Duration.ofSeconds(2));
        }

        try {
            TimeUnit.SECONDS.sleep(5);
            return new Response.Success("as2", id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response.Failure(e);
        }
    }
}
