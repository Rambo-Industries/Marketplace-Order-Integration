package br.com.ramboindustries.processor;

import br.com.ramboindustries.entity.steps.BaseStep;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;

import java.util.List;

public interface Processor
{

    /**
     * The name of this processor
     */
    Status name();

    /**
     * Uses the data processes in those steps
     */
    List<Status> uses();

    /**
     * Apply the desired step and returned the generated steps
     * @param steps needed to execute this step method
     * @return new steps
     */
    Result apply(final List<? extends BaseStep> steps, final PartnerType partnerType);


}
