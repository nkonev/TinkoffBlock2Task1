package org.example;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

public class HandlerImpl implements Handler {

    private final Client client;

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {

        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Response>()) {
            var startedAt = Instant.now();
            var deadline = startedAt.plus(15, ChronoUnit.SECONDS);

            scope.fork(() -> client.getApplicationStatus1(id));
            scope.fork(() -> client.getApplicationStatus2(id));

            scope.joinUntil(deadline);

            var res = scope.result();
            switch (res) {
                case Response.Failure failure -> {
                    return new ApplicationStatusResponse.Failure(Duration.between(Instant.now(), startedAt), 1);
                }
                case Response.RetryAfter retryAfter -> {
                    return new ApplicationStatusResponse.Failure(Duration.between(Instant.now(), startedAt), 1);
                }
                case Response.Success success -> {
                    return new ApplicationStatusResponse.Success(success.applicationId(), success.applicationStatus());
                }
            }


        } catch (ExecutionException e) {
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        } catch (TimeoutException e) {
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        }
    }
}
