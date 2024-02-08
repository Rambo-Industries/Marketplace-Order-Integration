package br.com.ramboindustries.service;

import br.com.ramboindustries.entity.steps.GenerateStep;
import br.com.ramboindustries.entity.Integration;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.ResultType;
import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import br.com.ramboindustries.receiveintegration.IntegrationReceive;
import br.com.ramboindustries.repository.IntegrationRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntegrationService
{

    private static final int RECOVERABLE_TRIES_NUMBER = 10;

    private final Map<PartnerType, IntegrationReceive> onIntegration;
    private final IntegrationRepository repository;

    private final Map<Status, Processor> finiteMachine;
    private final EnumMap<Status, Status> transitions;

    public IntegrationService(
            final List<IntegrationReceive> integrations,
            final List<Processor> processors,
            final IntegrationRepository repository
    )
    {
        onIntegration = integrations
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        IntegrationReceive::partner,
                        Function.identity()
                    )
                );

        finiteMachine = processors
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Processor::name,
                                Function.identity()
                        )
                );

        this.repository = repository;

        transitions = new EnumMap<>(Status.class);
        transitions.put(Status.GENERATE, Status.RECEIVE);
        transitions.put(Status.RECEIVE, Status.FETCH);
        transitions.put(Status.FETCH, Status.CONVERT);
        transitions.put(Status.CONVERT, Status.VALIDATE);
        transitions.put(Status.VALIDATE, Status.SHIPPING_INTEGRATION);
        transitions.put(Status.SHIPPING_INTEGRATION, Status.ORDER_INTEGRATION);
        transitions.put(Status.ORDER_INTEGRATION, Status.FINISHED);
    }

    public void push(final GenerateStep generate)
    {
        final var partner = generate.getPartner();
        log.debug("Received a new integration: {}", generate);

        final var steps = onIntegration.get(generate.getPartner()).understand(generate);
        if (steps.isEmpty())
        {
            // Partner sent invalid data maybe? ...
            final var integration = new Integration(null, partner, List.of(Result.unrecoverable(generate)), LocalDateTime.now());
            integration.setStatus(Status.FAILED);
            repository.save(integration);
            log.error("An error occurred with integration: {}", generate);
            return;
        }

        // Every step is a new order. Also, we need to keep track of the step that generated those N steps ...
        for (final var step : steps)
        {
            repository.save(new Integration(null, partner, List.of(Result.advance(generate), Result.advance(step)), LocalDateTime.now()));
        }
    }

    // Runs in a loop, on a different thread
    @PostConstruct
    public void process()
    {
        final var myThread = new Thread(() ->
        {
            while (true)
            {
                try {
                    Thread.sleep(1000);

                    final var integrations = repository.next();
                    for (final var integration : integrations)
                    {

                        final Processor processor = finiteMachine.get(integration.getStatus());
                        if (Objects.isNull(processor))
                        {
                            // no processor found, so, just try to advance
                            integration.setStatus(transitions.get(integration.getStatus()));
                            repository.save(integration);
                            continue;
                        }

                        final var steps = integration.getExecutions()
                                .stream()
                                .filter(process -> process.type() == ResultType.ADVANCE)
                                .map(Result::step)
                                .filter(step -> processor.uses().stream().anyMatch(status -> status == step.getStatus()))
                                .toList();

                        final var result = processor.apply(steps, integration.getPartner());
                        checkResult(result, integration);
                        repository.save(integration);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        myThread.setDaemon(true);
        myThread.start();
    }

    private boolean isRecoverable(final List<Result> results)
    {
        if (results.size() < RECOVERABLE_TRIES_NUMBER)
        {
            return true;
        }
        // Check the last @recoverableTriesNumber steps, if they are error, then it is not recoverable
        for (int index = results.size() - RECOVERABLE_TRIES_NUMBER; index < results.size(); index++)
        {
            final var result = results.get(index);
            if (result.type() != ResultType.FAIL_RECOVERABLE)
            {
                return true;
            }
        }
        return false;
    }

    private void checkResult(final Result result, final Integration integration)
    {
        integration.getExecutions().add(result);
        switch (result.type())
        {
            case ADVANCE ->
            {
                // Advance to the next status
                integration.setStatus(transitions.get(integration.getStatus()));
            }
            case FAIL_UNRECOVERABLE ->
            {
                // The order reached an unrecoverable error
                integration.setStatus(Status.FAILED);
            }
            case FAIL_RECOVERABLE ->
            {

                // If the order is not recoverable ...
                if (!isRecoverable(integration.getExecutions()))
                {
                    log.warn("Order: {} reached the maximum number of retries ...", integration.getId());
                    integration.setStatus(Status.FAILED);
                }
            }
        }
    }




}
