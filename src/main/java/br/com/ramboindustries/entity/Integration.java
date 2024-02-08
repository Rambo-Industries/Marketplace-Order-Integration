package br.com.ramboindustries.entity;

import br.com.ramboindustries.enumeration.PartnerType;
import br.com.ramboindustries.enumeration.Status;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "integrations")
@Data
public class Integration
{
    @Id
    private final ObjectId id;
    private final PartnerType partner;
    private final List<Result> executions;
    private Status status;
    private final LocalDateTime createdAt;

    public Integration(ObjectId id, PartnerType partner, List<Result> executions, LocalDateTime createdAt) {
        this.id = id;
        this.partner = partner;
        this.executions = executions;
        this.createdAt = createdAt;
        status = Status.GENERATE;
    }
}
