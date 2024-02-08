package br.com.ramboindustries.receiveintegration;

import br.com.ramboindustries.entity.steps.GenerateStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.enumeration.PartnerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
class ZibBRIntegrationReceive implements IntegrationReceive
{

    @Override
    public PartnerType partner() {
        return PartnerType.ZIBBR;
    }

    @Override
    public List<ReceiveStep> understand(final GenerateStep generateStep) {
        /**
         * This partner sends the order in the webhook request
         */
        return List.of(new ReceiveStep(generateStep.getData()));
    }
}
