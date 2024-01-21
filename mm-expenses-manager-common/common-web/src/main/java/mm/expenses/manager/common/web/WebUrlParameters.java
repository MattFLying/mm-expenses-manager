package mm.expenses.manager.common.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Contains all web url parameters from the url path stored as a simple map with values as a list.
 */
@Getter
public class WebUrlParameters {

    private final Map<String, ParameterValue> parametersMapByName;

    public WebUrlParameters(final Map<String, String[]> parametersMapByName) {
        this.parametersMapByName = convertOriginalMapToExpectedMap(parametersMapByName);
    }

    /**
     * Gets the parameter value by its name or empty if parameter does not exists.
     *
     * @param parameterName - name of expected parameter
     * @return parameter value
     */
    public ParameterValue getValue(final String parameterName) {
        return parametersMapByName.getOrDefault(parameterName, ParameterValue.empty());
    }

    /**
     * Converts the original parameters map {@link Map<String, String[]>} into expected internally type {@link Map<String, ParameterValue>}.
     *
     * @param parametersMapByName - original parameters map
     * @return converted original map
     */
    private Map<String, ParameterValue> convertOriginalMapToExpectedMap(final Map<String, String[]> parametersMapByName) {
        if (MapUtils.isEmpty(parametersMapByName)) {
            return new HashMap<>();
        }
        return parametersMapByName.entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> {
                            final var value = entry.getValue();
                            if (ArrayUtils.isEmpty(value)) {
                                return ParameterValue.empty();
                            }
                            return ParameterValue.of(value);
                        },
                        (oldKey, newKey) -> oldKey,
                        HashMap::new
                ));
    }

    /**
     * Specific parameter value.
     * The default value type is {@link List<String>} with possibility to convert the value.
     */
    @Getter
    @RequiredArgsConstructor
    public static class ParameterValue {

        private final List<String> values;

        private ParameterValue() {
            this.values = new ArrayList<>();
        }

        /**
         * Returns empty parameter value as empty list.
         *
         * @return empty parameter value as empty list
         */
        public static ParameterValue empty() {
            return new ParameterValue();
        }

        /**
         * Returns parameter value converted from String array.
         *
         * @param parameters - parameter values as a String array
         * @return parameter value converted from String array
         */
        public static ParameterValue of(final String[] parameters) {
            return new ParameterValue(Arrays.asList(parameters));
        }

        /**
         * Returns the size of available parameter values.
         *
         * @return size of parameter values
         */
        public int size() {
            return values.size();
        }

        /**
         * If parameter value contains a single element then it will be converted to {@link Boolean} value otherwise returns null.
         *
         * @return value as a {@link Boolean} value or null
         */
        public Boolean asBoolean() {
            if (size() != 1) {
                return null;
            }
            return Boolean.valueOf(values.get(0));
        }

        /**
         * If parameter value contains a single element then it will be converted to {@link Integer} value otherwise returns null.
         *
         * @return value as a {@link Integer} value or null
         */
        public Integer asInteger() {
            if (size() != 1) {
                return null;
            }
            return Integer.valueOf(values.get(0));
        }

        /**
         * If parameter value contains a single element then it will be converted to {@link Long} value otherwise returns null.
         *
         * @return value as a {@link Long} value or null
         */
        public Long asLong() {
            if (size() != 1) {
                return null;
            }
            return Long.valueOf(values.get(0));
        }

        /**
         * If parameter value contains a single element then it will be converted to {@link Double} value otherwise returns null.
         *
         * @return value as a {@link Double} value or null
         */
        public Double asDouble() {
            if (size() != 1) {
                return null;
            }
            return Double.valueOf(values.get(0));
        }

    }

}
