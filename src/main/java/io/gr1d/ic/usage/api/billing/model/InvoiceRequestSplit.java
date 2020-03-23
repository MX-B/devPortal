package io.gr1d.ic.usage.api.billing.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class InvoiceRequestSplit {

	@NotEmpty
	@Size(max = 64)
	private String providerUuid;

	@NotEmpty
	@Size(max = 64)
	private String apiUuid;

	@NotEmpty
	@Size(max = 64)
	private String planUuid;

	@Positive
	private BigDecimal tenantValue;

	@Positive
	private BigDecimal providerValue;

}
