package io.gr1d.ic.usage.repository;

import io.gr1d.ic.usage.model.audit.Invoice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author sergio.filho@moneyex.io
 */
@Repository
public interface InvoiceRepository extends CrudRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

	Optional<Invoice> findByInvoiceId(String invoiceId);

}
