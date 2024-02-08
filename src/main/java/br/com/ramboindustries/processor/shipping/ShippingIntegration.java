package br.com.ramboindustries.processor.shipping;

import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.ShippingStep;
import br.com.ramboindustries.util.HttpResponseBodyHandler;
import com.fasterxml.jackson.databind.JsonNode;
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
class ShippingIntegration
{

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private static final String HOST = "http://localhost:8080";
    private final HttpClient client;

    public ShippingIntegration()
    {
        client = HttpClient
                .newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public Result doRequest(final String country, final String state, final String city, final String postalCode)
    {
        final String path = String.format(
                "shipping/api/deliveries/country/%s/state/%s/city/%s/postalcode/%s",
                country.toLowerCase(),
                state.toLowerCase(),
                city.toLowerCase(),
                postalCode
        );
        final var url = String.format("%s/%s", HOST, path);
        log.debug("Shipping Request: {}", path);
        try
        {
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();

            final var httpResponse = client.send(request, new HttpResponseBodyHandler<>(ShippingResponse.class, JsonNode.class));
            final var status = HttpStatus.resolve(httpResponse.statusCode());

            if (status.is2xxSuccessful())
            {
                return Result.advance(new ShippingStep.ShippingStepOk(httpResponse.body().getSuccess()));
            }
            final var body = httpResponse.body().getError();

            if (status.is4xxClientError())
            {
                if (HttpStatus.BAD_REQUEST == status || HttpStatus.NOT_FOUND == status)
                {
                    return Result.unrecoverable(new ShippingStep.ShippingStepNok(body));
                }
            }
            return Result.recoverable(new ShippingStep.ShippingStepNok(body));
        }
        catch( final HttpTimeoutException timeoutException )
        {
            return Result.recoverable(new ShippingStep.ShippingStepNok(null));
        }
        catch (final Exception exception)
        {
            log.error("Unexpected error with request: {}", path);
            throw new RuntimeException(exception);
        }

    }





}
