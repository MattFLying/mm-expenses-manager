package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrail;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@Schema(name = "ExchangeRatesTrailsPage", description = "Paged exchange rate trails response.")
@RequiredArgsConstructor
@JsonPropertyOrder({"content", "numberOfElements", "totalElements", "totalPages", "hasNext", "isFirst", "isLast"})
public class ExchangeRatesTrailsPage {

    private final Page<ExchangeRateTrail> pageContent;
    private final Collection<ExchangeRateTrailDto> content;

    public ExchangeRatesTrailsPage(@NotNull final Page<ExchangeRateTrail> pageContent) {
        this.pageContent = pageContent;
        this.content = pageContent.getContent().stream()
                .map(trail -> ExchangeRateTrailDto.builder()
                        .operation(trail.getOperation())
                        .state(trail.getState())
                        .date(trail.getDate())
                        .skipped(trail.getSkipped())
                        .evaluated(trail.getEvaluated())
                        .affectedIds(trail.getAffectedIds())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Schema(description = "Available trails response.")
    public Collection<ExchangeRateTrailDto> getContent() {
        return content;
    }

    @Schema(description = "Available trails response count.")
    public Integer getNumberOfElements() {
        return content.size();
    }

    @Schema(description = "Available trails total elements.")
    public Long getTotalElements() {
        return pageContent.getTotalElements();
    }

    @Schema(description = "Available trails total pages.")
    public Integer getTotalPages() {
        return pageContent.getTotalPages();
    }

    @Schema(description = "Defines if there is more available pages.")
    public Boolean getHasNext() {
        return pageContent.hasNext();
    }

    @Schema(description = "Defines if retrieved page is first.")
    public Boolean getIsFirst() {
        return pageContent.isFirst();
    }

    @Schema(description = "Defines if retrieved page is last.")
    public Boolean getIsLast() {
        return pageContent.isLast();
    }

}
