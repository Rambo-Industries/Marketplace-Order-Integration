package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.Status;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ValidateStep implements BaseStep
{

    private static final Status STATUS = Status.VALIDATE;

    private final List<String> data;

    @Override
    public Status getStatus() {
        return STATUS;
    }

    public List<String> getData() {
        return data;
    }

}
