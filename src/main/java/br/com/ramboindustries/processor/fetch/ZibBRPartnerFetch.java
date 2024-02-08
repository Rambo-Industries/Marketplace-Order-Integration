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

package br.com.ramboindustries.processor.fetch;

import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.enumeration.PartnerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/**
 * This partner, sends the order document in the webhook
 * For example:
 * {
 *     "customer": {
 *         ...
 *     },
 *     "skus": [ ... ]
 * }
 */
class ZibBRPartnerFetch implements PartnerFetch
{

    @Override
    public PartnerType partner()
    {
        return PartnerType.ZIBBR;
    }

    @Override
    public Result apply(final ReceiveStep receiveStep)
    {
        // Since this partner send the whole document, we just need to forward it to the next step
        return Result.advance(new FetchStep.NoOpFetchStep(receiveStep.getData()));
    }
}
