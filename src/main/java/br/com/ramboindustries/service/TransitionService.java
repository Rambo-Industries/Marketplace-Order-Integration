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

import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
class TransitionService
{

    private final Map<Status, Processor> statusImplementation;
    private final Map<Status, Status> transitionTable;

    public TransitionService(final List<Processor> processors)
    {
        statusImplementation = processors
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Processor::name,
                        Function.identity()
                    )
                );

        final var table = new EnumMap<Status, Status>(Status.class);
        table.put(Status.GENERATE, Status.RECEIVE);
        table.put(Status.RECEIVE, Status.FETCH);
        table.put(Status.FETCH, Status.CONVERT);
        table.put(Status.CONVERT, Status.VALIDATE);
        table.put(Status.VALIDATE, Status.SHIPPING_INTEGRATION);
        table.put(Status.SHIPPING_INTEGRATION, Status.ORDER_INTEGRATION);
        table.put(Status.ORDER_INTEGRATION, Status.FINISHED);

        this.transitionTable = Collections.unmodifiableMap(table);
    }

    public final Processor getProcessor(final Status status)
    {
        return statusImplementation.get(status);
    }

    public final Status nextStatus(final Status current)
    {
        return transitionTable.get(current);
    }


}

