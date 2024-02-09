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

package br.com.ramboindustries.repository;

import br.com.ramboindustries.entity.Integration;
import br.com.ramboindustries.enumeration.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@Component
public class IntegrationRepositoryImpl implements IntegrationRepository
{

    private final MongoTemplate mongoTemplate;
    private static final List<Status> NON_TERMINALS = Stream.of(Status.values()).filter(Predicate.not(Status::isTerminal)).toList();
    private static final Sort SORT_BY = Sort.by("createdAt").ascending();

    private record ProcessingLock(String user, String threadName, LocalDateTime acquired)
    {
        public ProcessingLock(final String user, final String threadName)
        {
            this(user, threadName, LocalDateTime.now());
        }
    }

    @Override
    public Optional<Integration> nextOrders()
    {
        final var q = Query.query(
                Criteria
                        .where("status")
                        .in(NON_TERMINALS)
                        .orOperator(List.of(
                                Criteria.where("lock").isNull(),
                                // The lock should not hold more than 5 minutes
                                Criteria.where("lock.acquired").gte(LocalDateTime.now().minusMinutes(5))
                        ))
        ).
                with(SORT_BY);

        final var update = new Update().set("lock", new ProcessingLock(System.getProperty("user.name", "undefined"), Thread.currentThread().getName()));
        final var options = new FindAndModifyOptions().upsert(false).returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(q, update, options, Integration.class));
    }

    @Override
    public void save(final Integration integration)
    {
        final var query = Query.query(Criteria.where("_id").is(integration.getId()));
        var update = new Update()
                .set("partner", integration.getPartner())
                .set("executions", integration.getExecutions())
                .set("status", integration.getStatus())
                .set("lock", null);
        if (Objects.isNull(integration.getId()))
        {
            update = update.set("create_at", LocalDateTime.now());
        }
        final var options = new FindAndModifyOptions().upsert(true).returnNew(true);
        mongoTemplate.findAndModify(query, update, options, Integration.class);
    }

}
