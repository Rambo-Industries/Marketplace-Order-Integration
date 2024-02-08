package br.com.ramboindustries.util;

import java.util.Objects;

public class OperationResult<S, E>
{
    private final S success;
    private final E error;

    private OperationResult(final S success, final E error)
    {
        this.success = success;
        this.error = error;
    }

    public static <S, E> OperationResult<S, E> success(final S success)
    {
        Objects.requireNonNull(success, "Success object must not be null!");
        return new OperationResult<>(success, null);
    }

    public static <S, E> OperationResult<S, E> error(final E error)
    {
        Objects.requireNonNull(error, "Error object must not be null!");
        return new OperationResult<>(null, error);
    }

    public S getSuccess()
    {
        return success;
    }

    public E getError()
    {
        return error;
    }

    public boolean isSuccess()
    {
        return Objects.isNull(error);
    }



}
