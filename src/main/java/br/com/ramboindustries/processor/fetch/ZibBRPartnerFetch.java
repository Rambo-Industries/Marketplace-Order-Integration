package br.com.ramboindustries.processor.fetch;


import br.com.ramboindustries.entity.steps.FetchStep;
import br.com.ramboindustries.entity.steps.ReceiveStep;
import br.com.ramboindustries.entity.Result;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/**
 * This partner, sends the order document in the webhook
 * For example:
 * {
 *     "customer": {
 *         ...
 *     },
 *     "skus": [ ... ]
 * }
 */
class ZibBRPartnerFetch implements PartnerFetch
{

    @Override
    public PartnerType partner()
    {
        return PartnerType.ZIBBR;
    }

    @Override
    public Result apply(final ReceiveStep receiveStep)
    {
        // Since this partner send the whole document, we just need to forward it to the next step
        return Result.advance(new FetchStep.NoOpFetchStep(receiveStep.getData()));
    }
}
