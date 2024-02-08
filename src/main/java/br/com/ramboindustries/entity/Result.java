package br.com.ramboindustries.entity;

import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.enumeration.ResultType;

import java.time.LocalDateTime;

public  record Result(
        BaseStep step,
        ResultType type,
        LocalDateTime executed
){

    private Result(final BaseStep step, final ResultType type)
    {
        this(step, type, LocalDateTime.now());
    }

    public static Result advance(final BaseStep step)
    {
        return new Result(step, ResultType.ADVANCE);
    }

    public static Result recoverable(final BaseStep step)
    {
        return new Result(step, ResultType.FAIL_RECOVERABLE);
    }

    public static Result unrecoverable(final BaseStep step)
    {
        return new Result(step, ResultType.FAIL_UNRECOVERABLE);
    }

}
