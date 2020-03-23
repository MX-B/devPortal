package io.gr1d.ic.usage.api.billing.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * An item that is being sold to an user. Can be either a physical or
 * a virtual good. Think of a shirt and a mobile game microtransaction.
 *
 * @author Rafael M. Lins
 */
@Data
public class InvoiceRequestItem {
    @NotEmpty
    @Size(max = 128)
    private String itemId;

    @NotEmpty
    @Size(max = 256)
    private String description;

    @Min(1)
    @Max(9999999)
    private long quantity;

    private BigDecimal unitValue;

    @Size(max = 256)
    private String endpoint;

    private String apiUuid;

    private Long hits;

    private String planUuid;

    private String providerUuid;

    private boolean virtualGood = true;

    public BigDecimal getValue() {
        return unitValue.multiply(BigDecimal.valueOf(quantity));
    }

}
