/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2024, Rambo Industries
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package br.com.ramboindustries.service;

import br.com.ramboindustries.entity.Integration;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.GenerateStep;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntegrationService
{

    private static final int RECOVERABLE_TRIES_NUMBER = 10;

    private final Map<PartnerType, IntegrationReceive> onIntegration;
    private final IntegrationRepository repository;
    private final TransitionService transition;
    private final Executor executor;

    public IntegrationService(
            final List<IntegrationReceive> integrations,
            final IntegrationRepository repository,
            final TransitionService transition
    )
    {
        onIntegration = integrations
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        IntegrationReceive::partner,
                        Function.identity()
                    )
                );
        this.repository = repository;
        this.transition = transition;
        final var scheduled  = Executors.newScheduledThreadPool(3);
        scheduled.scheduleWithFixedDelay(this::process, 0, 500, TimeUnit.MILLISECONDS);
        this.executor = scheduled;
    }

    public void push(final GenerateStep generate)
    {
        final var partner = generate.getPartner();
        log.debug("Received a new integration: {}", generate);

        final var steps = onIntegration.get(generate.getPartner()).understand(generate);
        if (steps.isEmpty())
        {
            // Partner sent invalid data maybe? ...
            final var integration = new Integration(null, partner, List.of(Result.unrecoverable(generate)));
            integration.setStatus(Status.FAILED);
            repository.save(integration);
            log.error("An error occurred with integration: {}", generate);
            return;
        }

        // Every step is a new order. Also, we need to keep track of the step that generated those N steps ...
        for (final var step : steps)
        {
            repository.save(new Integration(null, partner, List.of(Result.advance(generate), Result.advance(step))));
        }
    }

    public void process()
    {
        repository.nextOrders()
                .ifPresent(integration ->
                {
                    final Processor processor = transition.getProcessor(integration.getStatus());
                    if (Objects.isNull(processor))
                    {
                        // no processor found, so, just try to advance
                        integration.setStatus(transition.nextStatus(integration.getStatus()));
                        repository.save(integration);
                    }
                    else
                    {
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
                });
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
                integration.setStatus(transition.nextStatus(integration.getStatus()));
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
