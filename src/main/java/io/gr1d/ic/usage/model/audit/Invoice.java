package io.gr1d.ic.usage.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "tenant_realm")
    private String tenantRealm;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.PERSIST)
    private List<InvoiceItem> items;

    @OneToMany(mappedBy = "invoice", cascade = ALL, fetch = LAZY)
    private List<InvoiceSplit> split = new ArrayList<>();

    public InvoiceItem createItem(final SubscriptionResponse sub, final Long hits, final BigDecimal chargedAmount, final String endpoint) {

        final InvoiceItem invoiceItem = InvoiceItem.builder()
                .gatewayId(sub.getApi().getGateway().getUuid())
                .apiUuid(sub.getApi().getApiUuid())
                .subscriptionUuid(sub.getUuid())
                .endpoint(endpoint)
                .planUuid(sub.getPlan().getUuid())
                .hits(hits)
                .providerUuid(sub.getApi().getProvider().getUuid())
                .chargedAmount(chargedAmount)
                .invoice(this)
                .createdAt(LocalDateTime.now())
                .build();

        if (items == null) {
            items = new LinkedList<>();
        }
        items.add(invoiceItem);
        return invoiceItem;
    }

    public void addSplit(final InvoiceRequestSplit invoiceRequestSplit) {
        split.add(new InvoiceSplit(invoiceRequestSplit, this));
    }

}
