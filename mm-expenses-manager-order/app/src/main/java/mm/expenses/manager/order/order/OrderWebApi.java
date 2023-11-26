package mm.expenses.manager.order.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.api.WebApi;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderWebApi implements WebApi {
    FIND_ALL("", HttpMethod.GET, HttpStatus.OK),
    FIND_BY_ID("/{id}", HttpMethod.GET, HttpStatus.OK),

    CREATE("", HttpMethod.POST, HttpStatus.CREATED),
    UPDATE("/{id}", HttpMethod.PUT, HttpStatus.CREATED),

    DELETE("/{id}", HttpMethod.DELETE, HttpStatus.NO_CONTENT),
    DELETE_BY_IDS("/remove", HttpMethod.DELETE, HttpStatus.NO_CONTENT);

    public static final String BASE_URL = "/orders";

    private final String pathUrl;
    private final HttpMethod method;
    private final HttpStatus expectedSuccessfulStatus;

    @Override
    public String getBasePath() {
        return BASE_URL;
    }

}
