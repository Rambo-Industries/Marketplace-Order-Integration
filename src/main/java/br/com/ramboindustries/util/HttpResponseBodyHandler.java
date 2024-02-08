package br.com.ramboindustries.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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

        final CompletableFuture<T> result = new CompletableFuture<>();

        // BUFFER initial size is 1MiB
        private static final int BUFFER_INITIAL_SIZE = 1024 * 1024;

        // TODO
        //private byte[] buffer = new byte[BUFFER_INITIAL_SIZE];
        //private int bufferIndex = 0;

        private final List<Byte> buffer = new ArrayList<>(BUFFER_INITIAL_SIZE);

        private volatile Flow.Subscription subscription;


        private final Class<T> resultClass;//this.getClass().

        public CustomJsonBodySubscriber(final Class<T> clazz)

        {
            this.resultClass = clazz;
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
        public void onNext(List<ByteBuffer> items)
        {
            // TODO
            for (final var item : items)
            {
                //toAddSize += item.remaining();
                while (item.hasRemaining())
                {
                    buffer.add(item.get());
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
                // TODO
                final byte b[] = new byte[buffer.size()];
                for (int index = 0; index < buffer.size(); index++)
                {
                    b[index] = buffer.get(index);
                }

                final T object = MAPPER.readValue(b, resultClass);
                result.complete(object);
            } catch (final IOException exception)
            {
                result.completeExceptionally(exception);
            }
        }
    }

}
