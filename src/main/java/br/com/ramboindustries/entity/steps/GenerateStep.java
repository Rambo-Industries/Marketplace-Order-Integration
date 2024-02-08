package br.com.ramboindustries.entity.steps;

import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class GenerateStep implements BaseStep
{

    private final Document data;
    private final PartnerType partner;
    private static final Status STATUS = Status.GENERATE;


    @Override
    public Status getStatus()
    {
        return STATUS;
    }

    public Document getData()
    {
        return data;
    }

    public PartnerType getPartner()
    {
        return partner;
    }

    public static class HttpGenerateStep extends GenerateStep
    {
        private final Map<String, String> headers;

        public HttpGenerateStep(Document data, PartnerType partner, Map<String, String> headers)
        {
            super(data, partner);
            this.headers = Collections.unmodifiableMap(headers);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }

}
