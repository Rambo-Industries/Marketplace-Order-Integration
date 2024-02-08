package br.com.ramboindustries.controller;

import br.com.ramboindustries.entity.steps.GenerateStep;
import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(value = "/integrations")
@RestController
@RequiredArgsConstructor
class IntegrationController {

    private final IntegrationService service;

    @PostMapping(value = "/{partner}")
    public ResponseEntity<Void> push(
            @PathVariable("partner") final String partner,
            @RequestHeader Map<String, String> headers,
            @RequestBody final String json)
    {
        final var document = Document.parse(json);
        final var step = new GenerateStep.HttpGenerateStep(document, PartnerType.valueOf(partner), headers);
        service.push(step);
        return ResponseEntity.accepted().build();
    }



}
