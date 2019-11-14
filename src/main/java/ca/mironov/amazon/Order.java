package ca.mironov.amazon;

import org.slf4j.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@SuppressWarnings("ClassWithTooManyFields")
class Order {

    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    private final String id;
    private final LocalDate date;
    private final BigDecimal itemsSubtotal;
    private final BigDecimal shippingAndHandling;
    private final BigDecimal discount;
    private final BigDecimal environmentalHandlingFee;
    private final BigDecimal totalBeforeTax;
    private final BigDecimal hst;
    private final BigDecimal total;
    private final String items;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    Order(String id, LocalDate date,
          BigDecimal itemsSubtotal, BigDecimal shippingAndHandling, BigDecimal discount, BigDecimal environmentalHandlingFee,
          BigDecimal totalBeforeTax, BigDecimal hst, BigDecimal total, String items) {
        this.id = id;
        this.date = date;
        this.itemsSubtotal = itemsSubtotal;
        this.shippingAndHandling = shippingAndHandling;
        this.discount = discount;
        this.environmentalHandlingFee = environmentalHandlingFee;
        this.totalBeforeTax = totalBeforeTax;
        this.hst = hst;
        this.total = total;
        this.items = items;
        if (itemsSubtotal.subtract(discount).add(shippingAndHandling).add(environmentalHandlingFee).compareTo(totalBeforeTax) != 0) {
            logger.error("Order {}: itemsSubtotal {} - discount {} + shippingAndHandling {} + environmentalHandlingFee {} != totalBeforeTax {}, diff={}",
                    id, itemsSubtotal, discount, shippingAndHandling, environmentalHandlingFee, totalBeforeTax,
                    totalBeforeTax.subtract(itemsSubtotal.subtract(discount).add(shippingAndHandling).add(environmentalHandlingFee)));
            throw new IllegalArgumentException("itemsSubtotal - discount + shippingAndHandling + environmentalHandlingFee != totalBeforeTax: " + this);
        }
        if (totalBeforeTax.add(hst).compareTo(total) != 0) {
            logger.error("Order {}: totalBeforeTax {} + hst {} != total {}, diff={}", id, totalBeforeTax, hst, total, total.subtract(totalBeforeTax.add(hst)));
            throw new IllegalArgumentException("totalBeforeTax + hst != total: " + this);
        }
    }

    String getId() {
        return id;
    }

    LocalDate getDate() {
        return date;
    }

    BigDecimal getItemsSubtotal() {
        return itemsSubtotal;
    }

    BigDecimal getShippingAndHandling() {
        return shippingAndHandling;
    }

    BigDecimal getDiscount() {
        return discount;
    }

    BigDecimal getEnvironmentalHandlingFee() {
        return environmentalHandlingFee;
    }

    BigDecimal getTotalBeforeTax() {
        return totalBeforeTax;
    }

    BigDecimal getHst() {
        return hst;
    }

    BigDecimal getTotal() {
        return total;
    }

    String getItems() {
        return items;
    }


}
