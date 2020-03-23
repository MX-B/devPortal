package io.gr1d.ic.usage.controller;

import io.gr1d.auth.keycloak.LoggedUser;
import io.gr1d.core.controller.BaseController;
import io.gr1d.core.datasource.model.Gr1dPageable;
import io.gr1d.core.datasource.model.PageResult;
import io.gr1d.ic.usage.model.Execution;
import io.gr1d.ic.usage.model.ExecutionUser;
import io.gr1d.ic.usage.repository.ExecutionRepository;
import io.gr1d.ic.usage.repository.ExecutionUserRepository;
import io.gr1d.ic.usage.request.ExecutionRequest;
import io.gr1d.ic.usage.response.ExecutionResponse;
import io.gr1d.ic.usage.response.ExecutionUserResponse;
import io.gr1d.ic.usage.service.RoutineExecutor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.NotNull;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@Api(tags = "Execution")
@RequestMapping(path = "/executions")
public class ExecutionController extends BaseController {

    private final RoutineExecutor routineExecutor;
    private final ExecutionRepository executionRepository;
    private final ExecutionUserRepository executionUserRepository;
    private final LoggedUser loggedUser;

    @Autowired
    public ExecutionController(final RoutineExecutor routineExecutor,
                               final ExecutionRepository executionRepository,
                               final ExecutionUserRepository executionUserRepository,
                               final LoggedUser loggedUser) {
        this.routineExecutor = routineExecutor;
        this.executionRepository = executionRepository;
        this.executionUserRepository = executionUserRepository;
        this.loggedUser = loggedUser;
    }

    @And({
            @Spec(path = "referenceMonth", params = "reference_month", spec = Equal.class),
            @Spec(path = "status.name", params = "status", spec = Equal.class),
            @Spec(path = "startedBy", params = "started_by", spec = Equal.class),
            @Spec(path = "startTrigger", params = "start_trigger", spec = Equal.class),
            @Spec(path = "removedAt", params = "removed", spec = NotNull.class, constVal = "false")
    })
    private interface ExecutionSpec extends Specification<Execution> {

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "reference_month", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "status", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "started_by", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "start_trigger", dataType = "string", paramType = "query"),
    })
    @ApiOperation(nickname = "history", value = "List Executions", notes = "Returns a list with all registered executions", tags = "Execution")
    @RequestMapping(path = "/history", method = GET, produces = JSON)
    public PageResult<ExecutionResponse> executionHistory(final Gr1dPageable gr1dPageable, final ExecutionSpec spec) {
        log.info("Listing Executions {}", gr1dPageable);
        final Page<Execution> page = executionRepository.findAll(spec, gr1dPageable.toPageable());
        final List<ExecutionResponse> list = page.getContent().stream()
                .map(ExecutionResponse::new)
                .collect(toList());
        return PageResult.ofPage(page, list);
    }

    @And({
            @Spec(path = "execution.referenceMonth", params = "reference_month", spec = Equal.class),
            @Spec(path = "execution.uuid", params = "execution", spec = Equal.class),
            @Spec(path = "status.name", params = "status", spec = Equal.class),
            @Spec(path = "tenantRealm", params = "tenant_realm", spec = Equal.class),
            @Spec(path = "keycloakId", params = "keycloak_id", spec = Equal.class),
            @Spec(path = "removedAt", params = "removed", spec = NotNull.class, constVal = "false")
    })
    private interface ExecutionUserSpec extends Specification<ExecutionUser> {

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "reference_month", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "execution", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "status", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "tenant_realm", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "keycloak_id", dataType = "string", paramType = "query"),
    })
    @ApiOperation(nickname = "historyPerUser", value = "List Executions per User", notes = "Returns a list with all registered executions per user", tags = "Execution")
    @RequestMapping(path = "/history/user", method = GET, produces = JSON)
    public PageResult<ExecutionUserResponse> historyPerUser(final Gr1dPageable gr1dPageable, final ExecutionUserSpec spec) {
        log.info("Listing Executions per User {}", gr1dPageable);
        final Page<ExecutionUser> page = executionUserRepository.findAll(spec, gr1dPageable.toPageable());
        final List<ExecutionUserResponse> list = page.getContent().stream()
                .map(ExecutionUserResponse::new)
                .collect(toList());
        return PageResult.ofPage(page, list);
    }

    @ApiOperation(nickname = "execute", value = "Execute", notes = "Executes a contabilization", tags = "Execution")
    @RequestMapping(path = "/execute", method = POST, consumes = JSON, produces = JSON)
    public ResponseEntity<?> execute(@Valid @RequestBody final ExecutionRequest request) {
        log.info("Manually executing the Routine. User Requesting (id:{}, email:{})", loggedUser.getId(), loggedUser.getEmail());
        routineExecutor.execute(request.getReferenceMonth(), request.getChargeDate(),
                loggedUser.getId(), "POST /executions/execute", request.getDescription());
        return ResponseEntity.ok().build();
    }

}
