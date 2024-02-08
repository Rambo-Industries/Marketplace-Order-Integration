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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
@Slf4j
class ZibBRPartnerConvert implements PartnerConvert
{

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Function<String, LocalDate> LOCAL_DATE_FUNCTION = str -> LocalDate.parse(str, LOCAL_DATE_FORMATTER);
    private static final UnaryOperator<String> ONLY_NUMBERS = str -> str.replaceAll("\\D", "");

    @Override
    public PartnerType partner()
    {
        return PartnerType.ZIBBR;
    }

    @Override
    public PreOrder apply(final FetchStep fetchStep)
    {
        final var step = (FetchStep.NoOpFetchStep)fetchStep;
        return mapToOrder(step.getData());
    }

    private PreOrder.Customer mapToCustomer(final Document document)
    {
        final var name = String.format(
                "%s %s",
                document.getString("primeiro_nome"),
                document.getString("segundo_nome")
        );

        return new PreOrder.Customer(
                name,
                ObjectHelper.safeParse(document.getString("data_nascimento"), LOCAL_DATE_FUNCTION),
                ObjectHelper.safeParse(document.getString("cpf"), ONLY_NUMBERS)
        );
    }

    private PreOrder mapToOrder(final Document root)
    {
        final var customer = mapToCustomer(root.getEmbedded(List.of("cliente"), Document.class));
        final var address = mapToAddress(root.getEmbedded(List.of("endereco"), Document.class));
        final var partner = mapToPartner(root.getInteger("codigo_do_pedido"));
        final var skus = mapToSku(root.getList("itens", Integer.class));
        return new PreOrder(customer, address, partner, skus);
    }

    private PreOrder.Address mapToAddress(final Document document)
    {
        return new PreOrder.Address(
                document.getString("pais"),
                document.getString("unidade_federativa"),
                ObjectHelper.safeParse(document.getString("cep"), ONLY_NUMBERS),
                document.getString("cidade"),
                document.getInteger("numero"),
                document.getString("observacao")
        );
    }

    private PreOrder.Partner mapToPartner(final Integer order)
    {
        return new PreOrder.Partner(PartnerType.PROVOLY, order.toString());
    }

    private List<PreOrder.SkuQuantity> mapToSku(final List<Integer> skus)
    {
        return skus
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new PreOrder.SkuQuantity(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }


}
