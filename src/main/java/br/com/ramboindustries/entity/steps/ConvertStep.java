package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.entity.PreOrder;
import br.com.ramboindustries.enumeration.Status;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConvertStep implements BaseStep
{

    private final PreOrder order;
    private static final Status STATUS = Status.CONVERT;

    @Override
    public Status getStatus()
    {
        return STATUS;
    }

    public PreOrder getData()
    {
        return order;
    }

}
