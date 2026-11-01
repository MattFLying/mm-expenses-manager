package mm.expenses.manager.common.web.config;

import org.apache.commons.lang3.StringUtils;

/**
 * App Keycloak details.
 *
 * @param hostname host name of keycloak
 * @param port     port where keycloak is available
 * @param url      full url to keycloak
 * @param realm    realm used withing the application
 */
public record Keycloak(String hostname, String port, String url, String realm) {

    @Override
    public String url() {
        if (StringUtils.isBlank(url)) {
            return SecurityRequestConfig.DEFAULT_KEYCLOAK_URL;
        }
        return url;
    }

    @Override
    public String realm() {
        if (StringUtils.isBlank(realm)) {
            return SecurityRequestConfig.DEFAULT_KEYCLOAK_REALM;
        }
        return realm;
    }

}
