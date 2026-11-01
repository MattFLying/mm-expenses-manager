package mm.expenses.manager.keycloak;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Simple representation of environment variables to be used inside Keycloak SPI implementations
 * taken from docker variables.
 */
@Getter
@Setter
@NoArgsConstructor
public class InternalProperties {

    public String accountServiceUrl;
    public String originHeader;
    public String kafkaBootstrapServer;
    public Integer httpConnectionTimeoutInSeconds;

    /**
     * Clean up properties.
     */
    public final void clean() {
        setAccountServiceUrl(null);
        setOriginHeader(null);
        setKafkaBootstrapServer(null);
        setHttpConnectionTimeoutInSeconds(null);
    }

    /**
     * Gets environment variable of given name. If such variable does not exist returns expected default value instead.
     */
    public Object getEnvironmentVariable(final String variableName, final Object defaultValueWhenMissing) {
        var value = System.getenv(variableName);
        if (StringUtils.isBlank(value)) {
            return defaultValueWhenMissing;
        }
        return value;
    }

}
