package mm.expenses.manager.order.validator;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ValidatorMessage {

    private final String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

}
