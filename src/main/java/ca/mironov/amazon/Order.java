package ca.mironov.amazon;

import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@SuppressWarnings("ClassWithTooManyFields")
record Order(
        @NotNull String id,
        @NotNull LocalDate date,
        @NotNull BigDecimal itemsSubtotal,
        @NotNull BigDecimal shippingAndHandling,
        @NotNull BigDecimal discount,
        @NotNull BigDecimal environmentalHandlingFee,
        @NotNull BigDecimal totalBeforeTax,
        @NotNull BigDecimal importFeesDeposit,
        @NotNull BigDecimal hst,
        @NotNull BigDecimal total,
        @NotNull String items
) {

    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    static final BigDecimal FINANCIAL_ZERO = new BigDecimal("0.00");

    @SuppressWarnings("ConstructorWithTooManyParameters")
    Order(@NotNull String id, @NotNull LocalDate date,
          @NotNull BigDecimal itemsSubtotal, @NotNull BigDecimal shippingAndHandling, @NotNull BigDecimal discount,
          @NotNull BigDecimal environmentalHandlingFee,
          @NotNull BigDecimal totalBeforeTax, @NotNull BigDecimal importFeesDeposit, @NotNull BigDecimal hst,
          @NotNull BigDecimal total, @NotNull String items) {
        this.id = id;
        this.date = date;
        this.itemsSubtotal = itemsSubtotal;
        this.shippingAndHandling = shippingAndHandling;
        this.discount = discount;
        this.environmentalHandlingFee = environmentalHandlingFee;
        this.totalBeforeTax = totalBeforeTax;
        this.importFeesDeposit = importFeesDeposit;
        this.hst = hst;
        this.total = total;
        this.items = items;
        if (itemsSubtotal.subtract(discount).add(shippingAndHandling).add(environmentalHandlingFee).compareTo(totalBeforeTax) != 0) {
            logger.error("Order {}: itemsSubtotal {} - discount {} + shippingAndHandling {} + environmentalHandlingFee {} != totalBeforeTax {}, diff={}",
                    id, itemsSubtotal, discount, shippingAndHandling, environmentalHandlingFee, totalBeforeTax,
                    totalBeforeTax.subtract(itemsSubtotal.subtract(discount).add(shippingAndHandling).add(environmentalHandlingFee)));
            throw new IllegalArgumentException("itemsSubtotal - discount + shippingAndHandling + environmentalHandlingFee != totalBeforeTax: " + this);
        }
        if (totalBeforeTax.add(importFeesDeposit).add(hst).compareTo(total) != 0) {
            logger.error("Order {}: totalBeforeTax {} + hst {} != total {}, diff={}", id, totalBeforeTax, hst, total, total.subtract(totalBeforeTax.add(hst)));
            throw new IllegalArgumentException("totalBeforeTax + hst != total: " + this);
        }
    }

}
