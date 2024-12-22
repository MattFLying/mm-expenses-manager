package mm.expenses.manager.order.client;

import lombok.*;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Generated
@FeignClient(name = "${app.feign-client.finance.name}", url = "${app.feign-client.finance.url}")
public interface FinanceApiClient {

    @PostMapping(value = "calculations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<CurrencyConversionResponse> convertMultipleRates(@RequestBody List<CurrencyConversionRequest> request);

}
