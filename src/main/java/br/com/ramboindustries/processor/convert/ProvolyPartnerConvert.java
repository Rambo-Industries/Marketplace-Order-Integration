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

package br.com.ramboindustries.processor.convert;

import br.com.ramboindustries.entity.PreOrder;
import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Component
@Slf4j
class ProvolyPartnerConvert implements PartnerConvert
{

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Function<String, LocalDate> LOCAL_DATE_FUNCTION = str -> LocalDate.parse(str, LOCAL_DATE_FORMATTER);

    @Override
    public PartnerType partner()
    {
        return PartnerType.PROVOLY;
    }

    @Override
    public PreOrder apply(final FetchStep fetchStep)
    {
        final var step = (FetchStep.HttpFetchStep) fetchStep;
        return mapToOrder(step.getResponse().getEmbedded(List.of("body"), Document.class));
    }

    private PreOrder.Customer mapToCustomer(final Document document)
    {
        return new PreOrder.Customer(
                document.getString("full_name"),
                ObjectHelper.safeParse(document.getString("birth_date"), LOCAL_DATE_FUNCTION),
                document.getString("document")
        );
    }

    private PreOrder mapToOrder(final Document root)
    {
        final var customer = mapToCustomer(root.getEmbedded(List.of("customer"), Document.class));
        final var address = mapToAddress(root.getEmbedded(List.of("shipping"), Document.class));
        final var partner = mapToPartner(root.getEmbedded(List.of("order"), Document.class).getInteger("code"));
        final var skus = mapToSku(root.getEmbedded(List.of("data"), Document.class).getList("skus", Document.class));
        return new PreOrder(customer, address, partner, skus);
    }

    private PreOrder.Address mapToAddress(final Document document)
    {
        return new PreOrder.Address(
                document.getString("country"),
                document.getString("state"),
                ObjectHelper.safeParse(document.getInteger("postal_code"), String::valueOf),
                document.getString("city"),
                document.getInteger("number"),
                document.getString("note")
        );
    }

    private PreOrder.Partner mapToPartner(final Integer order)
    {
        return new PreOrder.Partner(PartnerType.PROVOLY, order.toString());
    }

    private List<PreOrder.SkuQuantity> mapToSku(final List<Document> skus)
    {
        return skus
                .stream()
                .map(document -> new PreOrder.SkuQuantity(document.getInteger("code"), document.getInteger("quantity")))
                .toList();
    }

}
