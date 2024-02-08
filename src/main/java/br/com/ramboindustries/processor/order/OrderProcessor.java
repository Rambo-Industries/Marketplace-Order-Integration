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
