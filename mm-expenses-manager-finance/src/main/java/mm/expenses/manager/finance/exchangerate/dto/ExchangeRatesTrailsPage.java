package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrail;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

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

    public Collection<ExchangeRateTrailDto> getContent() {
        return content;
    }

    public Integer getNumberOfElements() {
        return content.size();
    }

    public Long getTotalElements() {
        return pageContent.getTotalElements();
    }

    public Integer getTotalPages() {
        return pageContent.getTotalPages();
    }

    public Boolean getHasNext() {
        return pageContent.hasNext();
    }

    public Boolean getIsFirst() {
        return pageContent.isFirst();
    }

    public Boolean getIsLast() {
        return pageContent.isLast();
    }

}
