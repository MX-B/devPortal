package io.gr1d.ic.usage.repository;

import io.gr1d.ic.usage.model.ExecutionUser;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExecutionUserRepository extends CrudRepository<ExecutionUser, Long>, JpaSpecificationExecutor<ExecutionUser> {

    @Query("SELECT count(eu) FROM ExecutionUser eu JOIN eu.execution exec " +
           "WHERE eu.tenantRealm = :tenantRealm " +
           "  AND eu.keycloakId = :keycloakId " +
           "  AND eu.status.id = 2" +
           "  AND exec.referenceMonth = :referenceMonth ")
    long countSuccessExecutionForUser(@Param("referenceMonth") Integer referenceMonth,
                                      @Param("tenantRealm") String tenantRealm,
                                      @Param("keycloakId") String keycloakId);

}
