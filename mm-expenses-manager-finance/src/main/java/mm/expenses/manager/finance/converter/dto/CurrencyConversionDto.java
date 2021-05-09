package mm.expenses.manager.finance.converter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "CurrencyConversionDto", description = "Currency conversion response.")
@Getter
@Builder
@RequiredArgsConstructor
@JsonPropertyOrder({"from", "to"})
public class CurrencyConversionDto {

    @Schema(description = "Id to be used for calculated result and retrieved with it. Just for recognition purposes, id won't be processed.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String id;

    @Schema(description = "Specific date for which calculation has been made.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final LocalDate date;

    @Schema(description = "Converted from.")
    private final CurrencyConversionValueDto from;

    @Schema(description = "Converted to.")
    private final CurrencyConversionValueDto to;

    @Schema(name = "CurrencyConversionValueDto", description = "Currency conversion value.")
    @Getter
    @Builder
    @RequiredArgsConstructor
    @JsonPropertyOrder({"value", "code"})
    public static class CurrencyConversionValueDto {

        @Schema(description = "Conversion value.")
        private final BigDecimal value;

        @Schema(description = "Conversion currency code.")
        private final String code;

    }

}
