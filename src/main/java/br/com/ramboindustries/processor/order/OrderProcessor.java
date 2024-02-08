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

package br.com.ramboindustries.processor.order;

import br.com.ramboindustries.entity.*;
import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.steps.ConvertStep;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.ShippingStep;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import br.com.ramboindustries.processor.shipping.ShippingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderProcessor implements Processor
{

    private final OrderIntegration integration;

    @Override
    public Status name() {
        return Status.ORDER_INTEGRATION;
    }

    @Override
    public List<Status> uses() {
        return List.of(Status.CONVERT, Status.SHIPPING_INTEGRATION);
    }

    @Override
    public Result apply(final List<? extends BaseStep> steps, final PartnerType partner)
    {

        PreOrder preOrder = null;
        ShippingResponse shipping = null;

        for (final var step : steps)
        {
            if (step instanceof ConvertStep c)
            {
                preOrder = c.getData();
            }

            if (step instanceof ShippingStep.ShippingStepOk s)
            {
                shipping = s.getData();
            }
        }

        if (Objects.isNull(preOrder) || Objects.isNull(shipping))
        {
            throw new IllegalArgumentException("Invalid state!");
        }

        preOrder.setShipping(new PreOrder.Shipping(shipping.deliveries().get(0).code(), shipping.deliveries().get(0).value()));
        preOrder.setTotalValue(preOrder.getValue() + preOrder.getShipping().value());
        return integration.doRequest(preOrder);
    }

}
