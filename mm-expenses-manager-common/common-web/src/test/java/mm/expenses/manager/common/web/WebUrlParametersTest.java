package mm.expenses.manager.common.web;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class WebUrlParametersTest {

    private static final String KEY = "test-key";
    private static final String[] VALUE = new String[]{"test-value-1", "test-value-2"};

    @Test
    void getParametersMapByName_mapHasContent_test() {
        // given
        final var params = new HashMap<String, String[]>();
        params.put(KEY, VALUE);

        final var webParams = new WebUrlParameters(params);

        // when
        final var result = webParams.getParametersMapByName();

        // then
        assertThat(result).isNotEmpty().containsOnlyKeys(KEY);
    }

    @Test
    void getValue_valueExists_test() {
        // given
        final var params = new HashMap<String, String[]>();
        params.put(KEY, VALUE);

        final var webParams = new WebUrlParameters(params);

        // when
        final var result = webParams.getValue(KEY);

        // then
        assertThat(result.getValues()).containsExactlyInAnyOrderElementsOf(Arrays.asList(VALUE));
        assertThat(webParams.getParametersMapByName()).containsKey(KEY);
    }

    @Test
    void getValue_keyDoesNotExists_emptyListValue_test() {
        // given
        final var params = new HashMap<String, String[]>();
        params.put(KEY, VALUE);

        final var webParams = new WebUrlParameters(params);

        // when
        final var unknownKey = "unknown-key";
        final var result = webParams.getValue(unknownKey);

        // then
        assertThat(result.getValues()).isEmpty();
        assertThat(webParams.getParametersMapByName()).doesNotContainKey(unknownKey);
        assertThat(webParams.getParametersMapByName()).containsOnlyKeys(KEY);
    }

    @Test
    void getValue_nullValuesExpectsEmptyListValue_emptyListValue_test() {
        // given
        final var nullKey = "null-key";
        final var params = new HashMap<String, String[]>();
        params.put(KEY, VALUE);
        params.put(nullKey, null);

        final var webParams = new WebUrlParameters(params);

        // when
        final var result = webParams.getValue(nullKey);

        // then
        assertThat(result.getValues()).isNotNull().isEmpty();
        assertThat(webParams.getParametersMapByName()).containsOnlyKeys(KEY, nullKey);
    }

}