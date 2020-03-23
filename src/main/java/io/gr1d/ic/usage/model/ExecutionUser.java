package io.gr1d.ic.usage.model;

import io.gr1d.core.datasource.model.BaseModel;
import io.gr1d.ic.usage.model.audit.Invoice;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter@Setter
@Table(name = "execution_user")
public class ExecutionUser extends BaseModel {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "execution_id")
    private Execution execution;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private ExecutionStatus status;

    @Column(name = "tenant_realm")
    private String tenantRealm;

    @Column(name = "keycloak_id")
    private String keycloakId;

    @Size(max = 1024)
    @Column(name = "error_reason")
    private String errorReason;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    public void setErrorReason(final String errorReason) {
        if (!StringUtils.isEmpty(errorReason)) {
            this.errorReason = errorReason.substring(0, Math.min(errorReason.length(), 1024));
        } else {
            this.errorReason = errorReason;
        }
    }

    @Override
    protected String uuidBase() {
        return "EXU";
    }

}
