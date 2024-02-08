package br.com.ramboindustries.processor.fetch;

import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.enumeration.PartnerType;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
@Slf4j
/**
 * This partner, send the data, which we later need to execute a http call to retrieve the order information
 * For example:
 * {
 *     "orders": [ 1, 2, 3 ... 10 ]
 * }
 */
class ProvolyPartnerFetch implements PartnerFetch
{
    private static final String ACCESS_TOKEN = "Bearer " + Base64.getEncoder().encodeToString("This is my access token ...".getBytes(StandardCharsets.UTF_8));

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private static final String HOST = "http://localhost:8080";
    private final HttpClient client;

    public ProvolyPartnerFetch() {
        client = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Override
    public PartnerType partner()
    {
        return PartnerType.PROVOLY;
    }

    @Override
    /**
     * In this partner, with one step, we create N steps ...
     */
    public Result apply(final ReceiveStep receiveStep)
    {
        final var order = receiveStep.getData().getInteger("order");
        final var path = String.format("provoly/api/orders/%d", order);
        final var url = String.format("%s/%s", HOST, path);


        final var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", ACCESS_TOKEN)
                .GET()
                .build();

        final var requestDocument = new Document()
                .append("uri", url)
                .append("method", "GET")
                .append("headers", new Document().append("Authorization", ACCESS_TOKEN));

        try {

            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final var family = response.statusCode() / 100;
            final var body = Document.parse(response.body());
            final var responseDocument = new Document()
                    .append("status", response.statusCode())
                    .append("headers", response.headers().map())
                    .append("body", body);

            switch (family)
            {
                case 2 ->
                {

                    return Result.advance(new FetchStep.HttpFetchStep(requestDocument, responseDocument));
                }
                case 4 ->
                {
                    // This a type of error that we can not recover from ...
                    if (response.statusCode() == 400 || response.statusCode() == 404)
                    {
                        return Result.unrecoverable(new FetchStep.HttpFetchStep(requestDocument, responseDocument));
                    }
                    else
                    {
                        // generate with the same "contract"
                        // So, in the later retry, we can fall back to this same code here
                        return Result.recoverable(new FetchStep.HttpFetchStep(requestDocument, responseDocument));
                    }
                }
                case 5 -> {
                    // generate with the same "contract"
                    // So, in the later retry, we can fall back to this same code here
                    return Result.recoverable(new FetchStep.HttpFetchStep(requestDocument, responseDocument));

                }
                default -> throw new RuntimeException("Unexpected http status code family returned!");
            }

        }
        catch (final HttpTimeoutException timeoutException)
        {
            log.warn("Timeout while executing request", timeoutException);
            final var document = new Document().append("message", "timeout from partner!").append("stacktrace", timeoutException.getMessage());
            return Result.recoverable(new FetchStep.HttpFetchStep(requestDocument, document));
        }
        catch (final Exception exception)
        {
            log.warn("Unhandled exception ...");
            throw new RuntimeException(exception);
        }
    }

}
