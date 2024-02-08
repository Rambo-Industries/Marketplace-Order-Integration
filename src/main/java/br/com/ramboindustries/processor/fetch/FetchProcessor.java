package br.com.ramboindustries.processor.fetch;

import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.entity.Result;
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
public class FetchProcessor implements Processor
{
    private final Map<PartnerType, PartnerFetch> strategies;

    public FetchProcessor(final List<PartnerFetch> partners) {
        this.strategies = partners
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        PartnerFetch::partner,
                        Function.identity()
                ));
    }

    @Override
    public Status name() {
        return Status.FETCH;
    }

    @Override
    public List<Status> uses()
    {
        return List.of(Status.RECEIVE);
    }

    @Override
    public Result apply(final List<? extends BaseStep> steps, final PartnerType partner) {

        log.debug("Steps: {}, partner: {}", steps, partner);

        final var function = strategies.get(partner);
        if (Objects.isNull(function))
        {
            log.warn("No strategy found to process partner: {}", partner);
            throw new RuntimeException("Should not came here ...");
        }
        // MUST be the only step!
        final var receiveStep = ((ReceiveStep) steps.get(0));
        return function.apply(receiveStep);
    }

}
