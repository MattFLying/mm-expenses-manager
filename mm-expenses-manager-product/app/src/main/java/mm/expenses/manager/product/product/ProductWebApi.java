package mm.expenses.manager.product.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.api.WebApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductWebApi implements WebApi {
    FIND_ALL(StringUtils.EMPTY, HttpMethod.GET, HttpStatus.OK),
    FIND_BY_ID(WebApi.ID_URL, HttpMethod.GET, HttpStatus.OK),

    CREATE(StringUtils.EMPTY, HttpMethod.POST, HttpStatus.CREATED),
    UPDATE(WebApi.ID_URL, HttpMethod.PATCH, HttpStatus.OK),

    DELETE(WebApi.ID_URL, HttpMethod.DELETE, HttpStatus.NO_CONTENT);

    public static final String BASE_URL = "/products";

    private final String pathUrl;
    private final HttpMethod method;
    private final HttpStatus expectedSuccessfulStatus;

    @Override
    public String getBasePath() {
        return BASE_URL;
    }

}
