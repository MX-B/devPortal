package io.gr1d.ic.usage.model;

import io.gr1d.core.datasource.model.BaseEnum;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "execution_status")
public class ExecutionStatus extends BaseEnum {

    public static final ExecutionStatus STARTED = new ExecutionStatus(1L, "STARTED");
    public static final ExecutionStatus SUCCESS = new ExecutionStatus(2L, "SUCCESS");
    public static final ExecutionStatus ERROR = new ExecutionStatus(3L, "ERROR");

    private ExecutionStatus(final Long id, final String name) {
        setId(id);
        setName(name);
    }

}
