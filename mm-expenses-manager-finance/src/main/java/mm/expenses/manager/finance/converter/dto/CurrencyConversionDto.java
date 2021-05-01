package mm.expenses.manager.finance.converter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@RequiredArgsConstructor
@JsonPropertyOrder({"from", "to"})
public class CurrencyConversionDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final LocalDate date;

    private final CurrencyConversionValueDto from;
    private final CurrencyConversionValueDto to;

    @Getter
    @Builder
    @RequiredArgsConstructor
    @JsonPropertyOrder({"value", "code"})
    public static class CurrencyConversionValueDto {

        private final BigDecimal value;
        private final CurrencyCode code;

    }

}
