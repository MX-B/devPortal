package io.gr1d.ic.usage.api.billing.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class InvoiceResponse {
	private Boolean created;
	private String uuid;
	private BigDecimal value;
	private String paymentStatus;
	private LocalDateTime createdAt;
	private LocalDate expirationDate;
}
