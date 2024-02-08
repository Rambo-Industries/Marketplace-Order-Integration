package br.com.ramboindustries.processor.convert;

import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.steps.ConvertStep;
import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConvertProcessor implements Processor
{

    private final Map<PartnerType, PartnerConvert> strategies;

    public ConvertProcessor(final List<PartnerConvert> partners) {
        this.strategies = partners
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        PartnerConvert::partner,
                        Function.identity()
                ));
    }

    @Override
    public Status name() {
        return Status.CONVERT;
    }

    @Override
    public List<Status> uses() {
        return List.of(Status.FETCH);
    }




    /**
     * Must convert from partner document to a internal document
     */
    @Override
    public Result apply(final List<? extends BaseStep> steps, final PartnerType partner)
    {
        if (steps.size() != 1)
        {
            throw new IllegalArgumentException("Steps must be only the fetch!");
        }

        log.debug("Steps: {}, partner: {}", steps, partner);

        final var function = strategies.get(partner);
        if (Objects.isNull(function))
        {
            log.warn("No strategy found to process partner: {}", partner);
            throw new RuntimeException("Should not came here ...");
        }
        // MUST be the only step!
        final var fetch = ((FetchStep) steps.get(0));
        final var result = function.apply(fetch);
        return Result.advance(new ConvertStep(result));
    }
}
