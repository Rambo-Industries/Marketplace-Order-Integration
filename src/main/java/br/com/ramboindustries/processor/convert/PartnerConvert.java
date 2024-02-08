package br.com.ramboindustries.processor.convert;

import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.entity.PreOrder;
import br.com.ramboindustries.enumeration.PartnerType;

interface PartnerConvert
{

    PartnerType partner();

    PreOrder apply(final FetchStep fetch);

}
