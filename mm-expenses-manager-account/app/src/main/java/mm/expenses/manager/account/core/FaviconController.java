package mm.expenses.manager.account.core;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Special controller that handles issue with favicon error.
 */
@Hidden
@RestController
@RequiredArgsConstructor
class FaviconController {

    @GetMapping("favicon.ico")
    ResponseEntity<Void> returnNoFavicon() {
        return ResponseEntity.noContent().build();
    }

}
