package mm.expenses.manager.account.core.user;

import java.util.List;
import java.util.Map;

/**
 * Model of user to be created via different potential use cases.
 */
public interface CreateUserModel {

    String getType();

    String getEventId();

    String getRealmId();

    String getRealmName();

    Long getTime();

    String getUserId();

    String getEmail();

    String getUsername();

    Boolean getIsEmailVerified();

    Boolean getIsEnabled();

    Map<String, List<String>> getAttributes();

    Map<String, String> getDetails();

}
