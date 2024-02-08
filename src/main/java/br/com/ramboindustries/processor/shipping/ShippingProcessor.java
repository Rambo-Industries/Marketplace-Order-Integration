package br.com.ramboindustries.processor.shipping;

import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.steps.ConvertStep;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.Processor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShippingProcessor implements Processor
{

    private final ShippingIntegration shippingIntegration;

    @Override
    public Status name() {
        return Status.SHIPPING_INTEGRATION;
    }

    @Override
    public List<Status> uses() {
        return List.of(Status.CONVERT);
    }

    @Override
    public Result apply(final List<? extends BaseStep> steps, final PartnerType partner)
    {
        final var address = ((ConvertStep) steps.get(0)).getData().getAddress();
        return shippingIntegration.doRequest(
                address.country(),
                address.state(),
                address.city(),
                address.postalCode()
        );
    }
}
