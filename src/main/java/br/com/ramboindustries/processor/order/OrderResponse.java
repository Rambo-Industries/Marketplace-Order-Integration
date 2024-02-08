package br.com.ramboindustries.processor.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(long code) { }
