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
