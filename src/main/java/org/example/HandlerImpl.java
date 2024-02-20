package org.example;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class HandlerImpl implements Handler {

    private final Client client;

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {

        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<ApplicationStatusResponse>()) {
            var startedAt = Instant.now();
            var deadline = startedAt.plus(15, ChronoUnit.SECONDS);

            scope.fork(() -> getResult(() -> client.getApplicationStatus1(id), startedAt));
            scope.fork(() -> getResult(() -> client.getApplicationStatus2(id), startedAt));
            scope.joinUntil(deadline);

            return scope.result();
        } catch (ExecutionException e) {
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        } catch (TimeoutException e) {
            return new ApplicationStatusResponse.Failure(Duration.ZERO, 0);
        }
    }

    private ApplicationStatusResponse getResult(Supplier<Response> supplier, Instant startedAt) throws InterruptedException {
        for (int i = 0; ; i++) {
            var res = supplier.get();
            switch (res) {
                case Response.Failure failure -> {
                    return new ApplicationStatusResponse.Failure(Duration.between(startedAt, Instant.now()), i);
                }
                case Response.RetryAfter retryAfter -> {
                    Thread.sleep(retryAfter.delay());
                }
                case Response.Success success -> {
                    return new ApplicationStatusResponse.Success(success.applicationId(), success.applicationStatus());
                }
            }
        }
    }
}
