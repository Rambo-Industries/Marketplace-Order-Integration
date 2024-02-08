package br.com.ramboindustries.util;

import java.util.Objects;
import java.util.function.Function;

public final class ObjectHelper
{

    private ObjectHelper()
    {}

    public static <T, E> E safeParse(final T input, final Function<T, E> convert)
    {
        return safeParse(input, convert, null);
    }

    public static <T, E> E safeParse(final T input, final Function<T, E> convert, final E defaultValue)
    {
        if (Objects.isNull(input))
        {
            return defaultValue;
        }
        return convert.apply(input);
    }


}
