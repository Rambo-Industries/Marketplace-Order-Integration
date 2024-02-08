package br.com.ramboindustries.receiveintegration;

import br.com.ramboindustries.entity.steps.GenerateStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.enumeration.PartnerType;

import java.util.List;

public interface IntegrationReceive
{

    PartnerType partner();

    /**
     * A partner may send N orders in one requests
     * @param receiveStep
     * @return
     */
    List<ReceiveStep> understand(final GenerateStep receiveStep);

}
