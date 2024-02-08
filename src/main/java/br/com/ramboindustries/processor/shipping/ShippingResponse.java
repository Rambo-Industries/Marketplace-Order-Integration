package br.com.ramboindustries.processor.shipping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShippingResponse(
    List<Delivery> deliveries) {

    public record Delivery(
            String type,
            long code,
            double value
    ){}

}
