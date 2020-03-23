package io.gr1d.ic.usage.model;

import io.gr1d.core.datasource.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Getter@Setter
@Table(name = "execution")
public class Execution extends BaseModel {

    @NotNull
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @NotNull
    @Column(name = "reference_month")
    private Integer referenceMonth;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "status_id")
    private ExecutionStatus status;

    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "started_by", nullable = false, length = 64)
    private String startedBy;

    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "start_trigger", nullable = false, length = 64)
    private String startTrigger;

    @NotNull
    @Size(max = 256)
    @Column(nullable = false)
    private String description;

    @Column(name = "error_reason")
    @Size(max = 1024)
    private String errorReason;

    @Size(max = 1024)
    private String parameters;

    public void setErrorReason(final String errorReason) {
        if (!StringUtils.isEmpty(errorReason)) {
            this.errorReason = errorReason.substring(0, Math.min(errorReason.length(), 1024));
        } else {
            this.errorReason = errorReason;
        }
    }

    @Override
    protected String uuidBase() {
        return "EX";
    }

}
