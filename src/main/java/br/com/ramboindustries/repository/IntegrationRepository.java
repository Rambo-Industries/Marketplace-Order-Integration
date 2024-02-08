package br.com.ramboindustries.repository;

import br.com.ramboindustries.entity.Integration;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IntegrationRepository extends MongoRepository<Integration, ObjectId>
{

    @Query("{ 'status': { '$nin': ['FINISHED', 'FAILED', null] } }")
    List<Integration> next();

}
