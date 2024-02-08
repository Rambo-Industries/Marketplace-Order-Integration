package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public abstract class FetchStep implements BaseStep
{

    private static final Status STATUS = Status.FETCH;

    @Override
    public Status getStatus()
    {
        return STATUS;
    }

    @RequiredArgsConstructor
    @Getter
    public static class HttpFetchStep extends FetchStep
    {
        private final Document request;
        private final Document response;
    }

    @RequiredArgsConstructor
    @Getter
    public static class NoOpFetchStep extends FetchStep
    {
        private final Document data;
    }



}
