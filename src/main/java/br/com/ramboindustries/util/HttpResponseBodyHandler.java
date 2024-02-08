package br.com.ramboindustries.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * This class is used to handle json values
 * @param <S>
 * @param <E>
 */

public class HttpResponseBodyHandler<S, E> implements HttpResponse.BodyHandler<OperationResult<S, E>>
{

    private final Class<S> success;
    private final Class<E> error;

    public HttpResponseBodyHandler(final Class<S> success, final Class<E> error)
    {
        this.success = success;
        this.error = error;
    }

    @Override
    public HttpResponse.BodySubscriber<OperationResult<S, E>> apply(HttpResponse.ResponseInfo responseInfo)
    {
        final var status = HttpStatusCode.valueOf(responseInfo.statusCode());
        if (status.is2xxSuccessful())
        {
            return HttpResponse.BodySubscribers.mapping(new CustomJsonBodySubscriber<>(success), OperationResult::success);
        }
        return HttpResponse.BodySubscribers.mapping(new CustomJsonBodySubscriber<>(error), OperationResult::error);
    }

    private static class CustomJsonBodySubscriber<T> implements HttpResponse.BodySubscriber<T>
    {

        private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

        private final CompletableFuture<T> result = new CompletableFuture<>();

        private final ByteList byteList;

        private volatile Flow.Subscription subscription;


        private final Class<T> resultClass;//this.getClass().

        public CustomJsonBodySubscriber(final Class<T> clazz)

        {
            this.resultClass = clazz;
            byteList = new ByteList();
        }

        @Override
        public CompletionStage<T> getBody() {
            return result;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription)
        {
            if (this.subscription != null) {
                subscription.cancel();
                return;
            }
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final List<ByteBuffer> buffers)
        {
            for (final var buffer : buffers)
            {
                while (buffer.hasRemaining())
                {
                    final int remaining = buffer.remaining();
                    final var tempArray = new byte[remaining];
                    buffer.get(tempArray, 0, tempArray.length);
                    byteList.merge(tempArray);
                }
            }
        }

        @Override
        public void onError(Throwable throwable)
        {
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete()
        {
            try
            {
                final T object = MAPPER.readValue(byteList.get(), resultClass);
                result.complete(object);
            } catch (final IOException exception)
            {
                result.completeExceptionally(exception);
            }
        }
    }

}
