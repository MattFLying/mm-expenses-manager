package mm.expenses.manager.order.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndIsDeleted(final UUID id, final boolean isDeleted);

    List<Order> findAllByIdInAndIsDeleted(final Set<UUID> ids, final boolean isDeleted);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.is_deleted = false", nativeQuery = true)
    Page<Order> findAllNotDeleted(final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndNotDeleted(@Param("name") final String name, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByPriceSummaryLessThanAndNotDeleted(@Param("priceSummary") final BigDecimal price, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByPriceSummaryGreaterThanAndNotDeleted(@Param("priceSummary") final BigDecimal price, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndPriceSummaryLessThanAndNotDeleted(@Param("name") final String name, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndPriceSummaryGreaterThanAndNotDeleted(@Param("name") final String name, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) = :productsCount AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountAndNotDeleted(@Param("productsCount") final Integer productsCount, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) < :productsCount AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountLessThanAndNotDeleted(@Param("productsCount") final Integer productsCount, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) > :productsCount AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountGreaterThanAndNotDeleted(@Param("productsCount") final Integer productsCount, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND jsonb_array_length(o.products) = :productsCount AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndProductsCountAndNotDeleted(@Param("name") final String name, @Param("productsCount") final Integer productsCount, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND jsonb_array_length(o.products) = :productsCount AND cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndProductsCountAndPriceSummaryLessThanAndNotDeleted(@Param("name") final String name, @Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE o.name LIKE %:name% AND jsonb_array_length(o.products) = :productsCount AND cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByNameAndProductsCountAndPriceSummaryGreaterThanAndNotDeleted(@Param("name") final String name, @Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) = :productsCount AND cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountAndPriceSummaryLessThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) = :productsCount AND cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountAndPriceSummaryGreaterThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) < :productsCount AND cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountLessThanAndPriceSummaryLessThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) < :productsCount AND cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountLessThanAndPriceSummaryGreaterThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) > :productsCount AND cast(o.price_summary -> 'amount' as float) < :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountGreaterThanAndPriceSummaryLessThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT o.* FROM o_order o WHERE jsonb_array_length(o.products) > :productsCount AND cast(o.price_summary -> 'amount' as float) > :priceSummary AND o.is_deleted = false", nativeQuery = true)
    Page<Order> findByProductsCountGreaterThanAndPriceSummaryGreaterThanAndNotDeleted(@Param("productsCount") final Integer productsCount, @Param("priceSummary") final BigDecimal value, final Pageable pageable);

}
