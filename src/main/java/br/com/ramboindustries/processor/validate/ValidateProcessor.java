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

package br.com.ramboindustries.processor.validate;

import br.com.ramboindustries.entity.PreOrder;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.steps.ConvertStep;
import br.com.ramboindustries.entity.steps.ValidateStep;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ValidateProcessor implements Processor
{

    @Override
    public Status name() {
        return Status.VALIDATE;
    }

    @Override
    public List<Status> uses() {
        return List.of(Status.CONVERT);
    }

    @Override
    public Result apply(final List<? extends BaseStep> steps, final PartnerType partnerType) {

        final var order = ((ConvertStep) steps.get(0)).getData();
        if (Objects.isNull(order))
        {
            return Result.unrecoverable(new ValidateStep(List.of("Order was not informed!")));
        }
        final List<String> errors = validateOrder(order);
        errors.addAll(validateCustomer(order.getCustomer()));
        errors.addAll(validateAddress(order.getAddress()));
        errors.addAll(validatePartner(order.getPartner()));
        if (errors.isEmpty())
        {
            return Result.advance(new ValidateStep(List.of("Everything ok with the document!")));
        }
        return Result.unrecoverable(new ValidateStep(errors));
    }


    private List<String> validateCustomer(final PreOrder.Customer customer)
    {
        if (Objects.isNull(customer))
        {
            return List.of("Customer was not informed!");
        }
        final var errors = new ArrayList<String>();

        if (!StringUtils.hasText(customer.name()))
        {
            errors.add("Customer name was not informed!");
        }
        if (Objects.isNull(customer.birth()))
        {
            errors.add("Customer birth date was not informed!");
        }

        if (!StringUtils.hasText(customer.document()))
        {
            errors.add("Customer document was not informed!");
        }
        return errors;
    }

    private List<String> validateAddress(final PreOrder.Address address)
    {
        if (Objects.isNull(address))
        {
            return List.of("Address was not informed!");
        }
        final var errors = new ArrayList<String>();
        if (!StringUtils.hasText(address.city()))
        {
            errors.add("Address city was not informed!");
        }

        if (!StringUtils.hasText(address.country()))
        {
            errors.add("Address country was not informed!");
        }

        if (!StringUtils.hasText(address.state()))
        {
            errors.add("Address state was not informed!");
        }
        if (!StringUtils.hasText(address.postalCode()))
        {
            errors.add("Address postal was not informed!");
        }
        return errors;
    }

    private List<String> validatePartner(final PreOrder.Partner partner)
    {
        if (Objects.isNull(partner))
        {
            return List.of("Partner was not informed!");
        }
        final var errors = new ArrayList<String>();
        if (!StringUtils.hasText(partner.id()))
        {
            errors.add("Partner order code was not informed!");
        }
        if (Objects.isNull(partner.type()))
        {
            errors.add("Partner type was not informed!");
        }
        return errors;
    }

    private List<String> validateOrder(final PreOrder order)
    {
        final var errors = new ArrayList<String>();
        return errors;
    }



}
