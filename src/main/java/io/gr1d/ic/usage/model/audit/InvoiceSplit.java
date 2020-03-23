package io.gr1d.ic.usage.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "invoice_split")
public class InvoiceSplit {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "provider_uuid", length = 64)
    private String providerUuid;

    @Column(name = "api_uuid", nullable = false, length = 64)
    private String apiUuid;

    @Column(name = "plan_uuid", length = 64)
    private String planUuid;

    @Column(name = "tenant_value", length = 64)
    private BigDecimal tenantValue;

    @Column(name = "provider_value", length = 64)
    private BigDecimal providerValue;


    public InvoiceSplit(final InvoiceRequestSplit invoiceRequestSplit, final Invoice invoice) {
        this.invoice = invoice;
        this.providerUuid = invoiceRequestSplit.getProviderUuid();
        this.apiUuid = invoiceRequestSplit.getApiUuid();
        this.planUuid = invoiceRequestSplit.getPlanUuid();
        this.tenantValue = invoiceRequestSplit.getTenantValue();
        this.providerValue = invoiceRequestSplit.getProviderValue();
    }
}
