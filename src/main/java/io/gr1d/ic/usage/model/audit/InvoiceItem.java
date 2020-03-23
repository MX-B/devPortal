package io.gr1d.ic.usage.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Getter@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice_item")
public class InvoiceItem {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "gateway_id")
    private String gatewayId;

    @Column(name = "subscription_uuid")
    private String subscriptionUuid;

    @Column(name = "plan_uuid", nullable = false)
    private String planUuid;

    @Column(name = "api_uuid", nullable = false)
    private String apiUuid;

    @Column(name = "hits", nullable = false)
    private Long hits;

    @Column(name = "charged_amount", nullable = false)
    private BigDecimal chargedAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String endpoint;

    @Column(name = "provider_uuid")
    private String providerUuid;

}
