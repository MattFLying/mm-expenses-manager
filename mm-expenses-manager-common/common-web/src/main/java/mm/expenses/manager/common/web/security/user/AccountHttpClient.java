package mm.expenses.manager.common.web.security.user;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * In case if there is no accessible {@link UserSimpleInterface} implementation based on JPA database access
 * this bean is in use to fetch the user via internal REST API call to the account service.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(UserSimpleInterface.class)
public class AccountHttpClient implements UserSimpleInterface<UserSimpleInterface.SimpleUser> {

    private final RestClient restClient;

    public AccountHttpClient(final RestClient.Builder restClientBuilder, @Value("${app.feign-client.account.url:http://localhost:8187}") final String accountServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(accountServiceUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.set(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE))
                .build();
    }

    @Override
    public Optional<UserSimpleInterface.SimpleUser> findByKeycloakId(final UUID keycloakId) {
        try {
            var httpHeaders = new HttpHeaders();
            httpHeaders.add(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);

            return Optional.ofNullable(
                    restClient.get()
                            .uri(SecurityRequestConfig.API_INTERNAL_SYSTEM_USER_KEYCLOAK_PATH, keycloakId)
                            .retrieve()
                            .body(UserSimpleInterface.SimpleUser.class)
            );
        } catch (final Exception exception) {
            log.error("Cannot find the user: {}. Message: {}", keycloakId, exception.getMessage(), exception);
            return Optional.empty();
        }
    }

}
