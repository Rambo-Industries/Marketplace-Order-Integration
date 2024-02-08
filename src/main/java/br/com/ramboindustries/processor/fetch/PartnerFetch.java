package br.com.ramboindustries.processor.fetch;

import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.enumeration.PartnerType;

interface PartnerFetch
{
    PartnerType partner();

    Result apply(final ReceiveStep receiveStep);

}
