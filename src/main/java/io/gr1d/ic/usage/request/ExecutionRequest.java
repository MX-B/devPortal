package io.gr1d.ic.usage.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.*;
import java.time.LocalDate;

@ToString
@Getter@Setter
public class ExecutionRequest {

    @Past
    @NotNull
    @ApiModelProperty(required = true, value = "Date to use as reference month to retain metrics", example = "2018-11-01")
    private LocalDate referenceMonth;

    @NotNull
    @FutureOrPresent
    @ApiModelProperty(required = true, value = "Date to generate charges", example = "2019-01-10")
    private LocalDate chargeDate;

    @NotEmpty
    @Size(max = 256)
    @ApiModelProperty(required = true, value = "A reason why you want to manually execute")
    private String description;

}
