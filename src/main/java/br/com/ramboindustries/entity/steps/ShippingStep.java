package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.shipping.ShippingResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

public abstract class ShippingStep implements BaseStep
{
    private static final Status STATUS = Status.SHIPPING_INTEGRATION;


    @Override
    public Status getStatus() {
        return STATUS;
    }

    @RequiredArgsConstructor
    public static class ShippingStepOk extends ShippingStep
    {

        private final ShippingResponse data;

        public ShippingResponse getData()
        {
            return data;
        }
    }

    @RequiredArgsConstructor
    public static class ShippingStepNok extends ShippingStep
    {

        private final JsonNode data;

        public JsonNode getData()
        {
            return data;
        }
    }


}
