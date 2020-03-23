package io.gr1d.devportal.usage.controller;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import io.gr1d.devportal.usage.SpringTestApplication;
import io.gr1d.ic.usage.api.subscriptions.model.PlanEndpointResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.repository.InvoiceRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringTestApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
public class InvoiceControllerIntegrationTest {

    private static final String INVOICE_ID = "adff6bb4-0b1c-40de-888b-50c8a8af4675";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private Flyway flyway;

    @Before
    public void setUp() {
        FixtureFactoryLoader.loadTemplates("io.gr1d.devportal.usage.fixtures");
    }

    @After
    public void clean() throws IllegalArgumentException {
        flyway.clean();
    }

    @Test
    @FlywayTest
    public void testListInvoicesAndGet() throws Exception {
        final SubscriptionResponse subscription = Fixture.from(SubscriptionResponse.class).gimme("valid");
        final PlanEndpointResponse planEndpointResponse = Fixture.from(PlanEndpointResponse.class).gimme("valid");
        subscription.getPlan().getPlanEndpoints().add(planEndpointResponse);
        final String uri = "/invoice";
        final Invoice invoice = new Invoice();
        invoice.setValue(BigDecimal.valueOf(100.00));
        invoice.setTenantRealm(subscription.getTenant().getRealm());
        invoice.setInvoiceId(INVOICE_ID);
        invoice.setPeriodStart(LocalDate.of(2018, 10, 1));
        invoice.setPeriodEnd(LocalDate.of(2018, 10, 31));
        invoice.setKeycloakId(subscription.getUserId());
        invoice.setCreatedAt(LocalDateTime.of(2018, 11, 1, 14, 35, 19));

        invoice.createItem(subscription, 1823612L, BigDecimal.valueOf(100.00), planEndpointResponse.getEndpoint());

        invoiceRepository.save(invoice);

        mockMvc.perform(get(uri)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").isArray())
                .andExpect(jsonPath("content").value(hasSize(1)))
                .andExpect(jsonPath("content[0].keycloak_id").value(subscription.getUserId()))
                .andExpect(jsonPath("content[0].invoice_id").value(INVOICE_ID))
                .andExpect(jsonPath("content[0].period_start").value("2018-10-01"))
                .andExpect(jsonPath("content[0].period_end").value("2018-10-31"))
                .andExpect(jsonPath("content[0].created_at").value("2018-11-01T14:35:19"))
                .andExpect(jsonPath("content[0].value").value(100.00));

        mockMvc.perform(get(String.format("%s/%s", uri, INVOICE_ID))).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("keycloak_id").value(subscription.getUserId()))
                .andExpect(jsonPath("invoice_id").value(INVOICE_ID))
                .andExpect(jsonPath("period_start").value("2018-10-01"))
                .andExpect(jsonPath("period_end").value("2018-10-31"))
                .andExpect(jsonPath("created_at").value("2018-11-01T14:35:19"))
                .andExpect(jsonPath("value").value(100.00))
                .andExpect(jsonPath("tenant_realm").value(subscription.getTenant().getRealm()))
                .andExpect(jsonPath("items").isArray())
                .andExpect(jsonPath("items").value(hasSize(1)))
                .andExpect(jsonPath("items[0].plan_uuid").value(subscription.getPlan().getUuid()))
                .andExpect(jsonPath("items[0].api_uuid").value(subscription.getApi().getApiUuid()))
                .andExpect(jsonPath("items[0].subscription_uuid").value(subscription.getUuid()))
                .andExpect(jsonPath("items[0].gateway_id").value(subscription.getApi().getGateway().getUuid()))
                .andExpect(jsonPath("items[0].charged_amount").value(100.00))
                .andExpect(jsonPath("items[0].hits").value(1823612L))
                .andExpect(jsonPath("items[0].endpoint").value(subscription.getPlan().getPlanEndpoints().get(0).getEndpoint()))
                .andExpect(jsonPath("items[0].created_at").exists());
    }

}
