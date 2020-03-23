package io.gr1d.ic.usage.controller;

import io.gr1d.core.controller.BaseController;
import io.gr1d.core.datasource.model.Gr1dPageable;
import io.gr1d.core.datasource.model.PageResult;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.repository.InvoiceRepository;
import io.gr1d.ic.usage.response.InvoiceResponse;
import io.gr1d.ic.usage.service.InvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@Api(tags = "Audit Invoice")
@RequestMapping(path = "/invoice")
public class InvoiceController extends BaseController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceController(final InvoiceService invoiceService, final InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    @And({
            @Spec(path = "tenant_realm", params = "tenant_realm", spec = Equal.class),
            @Spec(path = "keycloak_id", params = "keycloak_id", spec = Equal.class),
            @Spec(path = "invoice_id", params = "invoice_id", spec = Equal.class)
    })
    private interface InvoiceSpec extends Specification<Invoice> {

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenant_realm", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "keycloak_id", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "invoice_id", dataType = "string", paramType = "query")
    })
    @ApiOperation(nickname = "invoices", value = "List Audit Invoices", notes = "Returns a list with all audit invoices", tags = "Audit Invoice")
    @RequestMapping(path = "", method = GET, produces = JSON)
    public PageResult<InvoiceResponse> list(final Gr1dPageable gr1dPageable, final InvoiceSpec spec) {
        log.info("Listing Invoices {}", gr1dPageable);
        final Page<Invoice> page = invoiceRepository.findAll(spec, gr1dPageable.toPageable());
        final List<InvoiceResponse> list = page.getContent().stream()
                .map(InvoiceResponse::new)
                .collect(toList());
        return PageResult.ofPage(page, list);
    }

    @ApiOperation(nickname = "invoice", value = "Get Audit Invoice Details", notes = "Returns a invoice details", tags = "Audit Invoice")
    @RequestMapping(path = "/{invoice_id}", method = GET, produces = JSON)
    public ResponseEntity<Invoice> find(@PathVariable("invoice_id") final String invoiceId) {
        log.info("Requesting Invoice {}", invoiceId);
        return invoiceService.findByInvoiceId(invoiceId).map(ResponseEntity.ok()::body)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
