package org.example;

import java.util.concurrent.TimeUnit;

public class ClientImpl implements Client {
    @Override
    public Response getApplicationStatus1(String id) {
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
        try {
            TimeUnit.SECONDS.sleep(8);
            return new Response.Success("as2", id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response.Failure(e);
        }
    }
}
