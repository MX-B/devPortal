package io.gr1d.ic.usage.service;

import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceService(final InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(final Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    public Optional<Invoice> findByInvoiceId(final String invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId);
    }

}
