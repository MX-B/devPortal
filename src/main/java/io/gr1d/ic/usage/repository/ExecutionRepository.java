package io.gr1d.ic.usage.repository;

import io.gr1d.ic.usage.model.Execution;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Rafael M. Lins
 *
 */
public interface ExecutionRepository extends CrudRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {
	
}
