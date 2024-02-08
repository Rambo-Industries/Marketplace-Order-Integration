package br.com.ramboindustries.receiveintegration;

import br.com.ramboindustries.entity.steps.GenerateStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.enumeration.PartnerType;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
class ProvolyIntegrationReceive implements IntegrationReceive
{

    @Override
    public PartnerType partner()
    {
        return PartnerType.PROVOLY;
    }

    @Override
    public List<ReceiveStep> understand(GenerateStep generateStep)
    {
        /**
         * This partner sends a list of orders that we need to fetch from their apis
        */
        return generateStep.getData().getList("orders", Integer.class, Collections.emptyList())
                .stream()
                .map(order -> {
                    final var document = new Document().append("order", order);
                    return new ReceiveStep(document);
                })
                .toList();
    }
}
