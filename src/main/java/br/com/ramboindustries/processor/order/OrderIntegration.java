package br.com.ramboindustries.processor.order;

import br.com.ramboindustries.entity.steps.OrderIntegrationStep;
import br.com.ramboindustries.entity.PreOrder;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.util.HttpResponseBodyHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Component
@Slf4j
class OrderIntegration
{

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private static final String HOST = "http://localhost:8080";
    private final HttpClient client;

    public OrderIntegration()
    {
        client = HttpClient
                .newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public Result doRequest(final PreOrder preOrder)
    {
        final var url = String.format("%s/%s", HOST, "orders/api");
        log.debug("Order Request: {}", url);

        try
        {

            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)

                    // TODO
                    .POST(HttpRequest.BodyPublishers.ofString(todo(preOrder)))
                    .build();

            final var httpResponse = client.send(request, new HttpResponseBodyHandler<>(OrderResponse.class, JsonNode.class));

            final var status = HttpStatus.resolve(httpResponse.statusCode());

            if (status.is2xxSuccessful())
            {
                return Result.advance(new OrderIntegrationStep.OrderIntegrationOk(httpResponse.body().getSuccess()));
            }
            final var body = httpResponse.body().getError();

            if (status.is4xxClientError())
            {
                if (HttpStatus.BAD_REQUEST == status || HttpStatus.NOT_FOUND == status)
                {
                    return Result.unrecoverable(new OrderIntegrationStep.OrderIntegrationNok(body));
                }
            }
            return Result.recoverable(new OrderIntegrationStep.OrderIntegrationNok(body));
        }
        catch( final HttpTimeoutException timeoutException )
        {
            return Result.recoverable(new OrderIntegrationStep.OrderIntegrationNok(null));
        }
        catch (final Exception exception)
        {
            log.error("Unexpected error with request: {}", url);
            throw new RuntimeException(exception);
        }
    }


    @SneakyThrows
    private String todo(final PreOrder order)
    {
        return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(order);
    }

}
