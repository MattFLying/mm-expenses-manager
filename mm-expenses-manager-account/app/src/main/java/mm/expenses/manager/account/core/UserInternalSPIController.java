package mm.expenses.manager.account.core;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.RegisterUserBySPI;
import mm.expenses.manager.account.core.user.UserService;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/spi")
class UserInternalSPIController {

    private final UserService service;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserResponse> registerUser(@RequestBody final RegisterUserBySPI request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.createDecorated(request));
    }

    @GetMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserResponse> findByKeycloakId(@PathVariable("id") final UUID keycloakId) {
        final var context = currentUserProvider.getSecurityContext();
        return ResponseEntity.ok(service.findByKeycloakIdMapped(context, keycloakId));
    }

}
