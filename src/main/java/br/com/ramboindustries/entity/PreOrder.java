package br.com.ramboindustries.entity;

import br.com.ramboindustries.enumeration.PartnerType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Data
public class PreOrder
{
    private final Customer customer;
    private final Address address;
    private final Partner partner;
    private final List<SkuQuantity> skus;

    private double value;

    private double totalValue;

    private Shipping shipping;


    public record Partner(
            PartnerType type,
            String id
    ) {
    }

    public record Customer(
            String name,
            LocalDate birth,
            String document
    ) {
    }

    public record Address(
            String country,
            String state,
            String postalCode,
            String city,
            int number,
            String note
    ) {
    }

    public record Shipping(
            long code,
            double value
    ){}

    public record SkuQuantity(
            int sku,
            int quantity
    ){}

}

