package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.Status;
import br.com.ramboindustries.processor.order.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

public abstract class OrderIntegrationStep implements BaseStep
{

    private static final Status STATUS = Status.ORDER_INTEGRATION;


    @Override
    public Status getStatus() {
        return STATUS;
    }

    @RequiredArgsConstructor
    public static class OrderIntegrationOk extends OrderIntegrationStep
    {

        private final OrderResponse data;

        public OrderResponse getData()
        {
            return data;
        }
    }

    @RequiredArgsConstructor
    public static class OrderIntegrationNok extends OrderIntegrationStep
    {

        private final JsonNode node;

        public JsonNode getData()
        {
            return node;
        }

    }

}
