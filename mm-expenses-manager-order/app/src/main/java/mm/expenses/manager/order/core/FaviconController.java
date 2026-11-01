package mm.expenses.manager.order.core;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
