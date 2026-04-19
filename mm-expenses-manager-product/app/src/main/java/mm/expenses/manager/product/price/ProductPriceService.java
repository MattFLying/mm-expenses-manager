package mm.expenses.manager.product.price;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProductPriceService {

    private final ProductPriceMapper mapper;

    public ProductPrice create(final CreateProductRequest request) {
        return mapper.map(request, LocalDate.now().toString());
    }

}
