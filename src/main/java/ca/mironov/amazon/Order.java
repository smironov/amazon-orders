package ca.mironov.amazon;

import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.StringJoiner;

@SuppressWarnings("ClassWithTooManyFields")
class Order {

    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    static final BigDecimal FINANCIAL_ZERO = new BigDecimal("0.00");

    private final @NotNull String id;
    private final @NotNull LocalDate date;
    private final @NotNull BigDecimal itemsSubtotal;
    private final @NotNull BigDecimal shippingAndHandling;
    private final @NotNull BigDecimal discount;
    private final @NotNull BigDecimal environmentalHandlingFee;
    private final @NotNull BigDecimal totalBeforeTax;
    private final @NotNull BigDecimal hst;
    private final @NotNull BigDecimal total;
    private final @NotNull String items;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    Order(@NotNull String id, @NotNull LocalDate date,
          @NotNull BigDecimal itemsSubtotal, @NotNull BigDecimal shippingAndHandling, @NotNull BigDecimal discount,
          @NotNull BigDecimal environmentalHandlingFee,
          @NotNull BigDecimal totalBeforeTax, @NotNull BigDecimal hst, @NotNull BigDecimal total, @NotNull String items) {
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

    @NotNull String getId() {
        return id;
    }

    @NotNull LocalDate getDate() {
        return date;
    }

    @NotNull BigDecimal getItemsSubtotal() {
        return itemsSubtotal;
    }

    @NotNull BigDecimal getShippingAndHandling() {
        return shippingAndHandling;
    }

    @NotNull BigDecimal getDiscount() {
        return discount;
    }

    @NotNull BigDecimal getEnvironmentalHandlingFee() {
        return environmentalHandlingFee;
    }

    @NotNull BigDecimal getTotalBeforeTax() {
        return totalBeforeTax;
    }

    @NotNull BigDecimal getHst() {
        return hst;
    }

    @NotNull BigDecimal getTotal() {
        return total;
    }

    @NotNull String getItems() {
        return items;
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (!id.equals(order.id)) return false;
        if (!date.equals(order.date)) return false;
        if (!itemsSubtotal.equals(order.itemsSubtotal)) return false;
        if (!shippingAndHandling.equals(order.shippingAndHandling)) return false;
        if (!discount.equals(order.discount)) return false;
        if (!environmentalHandlingFee.equals(order.environmentalHandlingFee)) return false;
        if (!totalBeforeTax.equals(order.totalBeforeTax)) return false;
        if (!hst.equals(order.hst)) return false;
        if (!total.equals(order.total)) return false;
        return items.equals(order.items);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + itemsSubtotal.hashCode();
        result = 31 * result + shippingAndHandling.hashCode();
        result = 31 * result + discount.hashCode();
        result = 31 * result + environmentalHandlingFee.hashCode();
        result = 31 * result + totalBeforeTax.hashCode();
        result = 31 * result + hst.hashCode();
        result = 31 * result + total.hashCode();
        result = 31 * result + items.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("date=" + date)
                .add("itemsSubtotal=" + itemsSubtotal)
                .add("shippingAndHandling=" + shippingAndHandling)
                .add("discount=" + discount)
                .add("environmentalHandlingFee=" + environmentalHandlingFee)
                .add("totalBeforeTax=" + totalBeforeTax)
                .add("hst=" + hst)
                .add("total=" + total)
                .add("items='" + items + "'")
                .toString();
    }

}
