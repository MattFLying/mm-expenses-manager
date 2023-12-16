package mm.expenses.manager.common.web.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Interface provides application default properties.
 */
public interface AppConfigProperties {

    /**
     * Gets application name.
     *
     * @return application name
     */
    String getName();

    /**
     * Gets application description.
     *
     * @return application description
     */
    String getDescription();

    /**
     * Gets application version.
     *
     * @return application version
     */
    String getVersion();

    /**
     * Gets contact information.
     *
     * @return contact information
     */
    Contact getContact();

    /**
     * Gets developer name together with the role.
     *
     * @return developer name together with the role
     */
    default String getDeveloperNameWithRole() {
        if (Objects.isNull(getContact())) {
            return StringUtils.EMPTY;
        }
        return String.format("%s - %s", getContact().name(), getContact().role());
    }

    /**
     * Gets developer's email.
     *
     * @return developer's email
     */
    default String getDeveloperEmail() {
        if (Objects.isNull(getContact())) {
            return StringUtils.EMPTY;
        }
        return getContact().email();
    }

}
