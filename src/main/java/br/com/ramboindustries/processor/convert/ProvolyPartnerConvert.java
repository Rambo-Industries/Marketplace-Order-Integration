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
