/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2024, Rambo Industries
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
