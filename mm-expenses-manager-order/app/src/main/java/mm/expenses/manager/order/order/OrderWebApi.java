package mm.expenses.manager.order.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.api.WebApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderWebApi implements WebApi {
    FIND_ALL(StringUtils.EMPTY, HttpMethod.GET, HttpStatus.OK),
    FIND_BY_ID("/{id}", HttpMethod.GET, HttpStatus.OK),

    CREATE(StringUtils.EMPTY, HttpMethod.POST, HttpStatus.CREATED),
    UPDATE("/{id}", HttpMethod.PATCH, HttpStatus.OK),

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
