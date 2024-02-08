package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.Status;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class ReceiveStep implements BaseStep
{

    private static final Status STATUS = Status.RECEIVE;

    private final Document data;

    @Override
    public Status getStatus() {
        return STATUS;
    }

    public Document getData() {
        return data;
    }


}
