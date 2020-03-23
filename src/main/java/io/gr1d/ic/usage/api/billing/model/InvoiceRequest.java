package io.gr1d.ic.usage.api.billing.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Someone is to be billed of what is described in here.
 * 
 * @author Rafael M. Lins
 *
 */
@Data
public class InvoiceRequest {
	@NotEmpty
	@Size(min = 1, max = 42)
	private String userId;
	
	private LocalDate chargeDate;
	private LocalDate expirationDate;
	private LocalDate periodStart;
	private LocalDate periodEnd;

	@NotEmpty
	@Size(min = 1, max = 42)
	private String tenantRealm;
	
	@NotEmpty
	@Size(max = 500)
	private Collection<InvoiceRequestItem> items;

	@NotEmpty
	@Size(max = 100)
	private Collection<InvoiceRequestSplit> split = new LinkedList<>();
	
}
