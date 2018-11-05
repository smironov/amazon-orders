package ca.mironov.amazon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Order {

    private final String id;
    private final LocalDate date;
    private final BigDecimal itemsSubtotal;
    private final BigDecimal shippingAndHandling;
    private final BigDecimal environmentalHandlingFee;
    private final BigDecimal totalBeforeTax;
    private final BigDecimal hst;
    private final BigDecimal total;
    private final String items;

    public Order(String id, LocalDate date,
                 BigDecimal itemsSubtotal, BigDecimal shippingAndHandling, BigDecimal environmentalHandlingFee,
                 BigDecimal totalBeforeTax, BigDecimal hst, BigDecimal total, String items) {
        this.id = id;
        this.date = date;
        this.itemsSubtotal = itemsSubtotal;
        this.shippingAndHandling = shippingAndHandling;
        this.environmentalHandlingFee = environmentalHandlingFee;
        this.totalBeforeTax = totalBeforeTax;
        this.hst = hst;
        this.total = total;
        this.items = items;
        if (itemsSubtotal.add(shippingAndHandling).add(environmentalHandlingFee).compareTo(totalBeforeTax) != 0) {
            throw new IllegalArgumentException("itemsSubtotal + shippingAndHandling != totalBeforeTax: " + this);
        }
        if (totalBeforeTax.add(hst).compareTo(total) != 0) {
            throw new IllegalArgumentException("totalBeforeTax + hst != total: " + this);
        }
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getItemsSubtotal() {
        return itemsSubtotal;
    }

    public BigDecimal getShippingAndHandling() {
        return shippingAndHandling;
    }

    public BigDecimal getEnvironmentalHandlingFee() {
        return environmentalHandlingFee;
    }

    public BigDecimal getTotalBeforeTax() {
        return totalBeforeTax;
    }

    public BigDecimal getHst() {
        return hst;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(date, order.date) &&
                Objects.equals(itemsSubtotal, order.itemsSubtotal) &&
                Objects.equals(shippingAndHandling, order.shippingAndHandling) &&
                Objects.equals(environmentalHandlingFee, order.environmentalHandlingFee) &&
                Objects.equals(totalBeforeTax, order.totalBeforeTax) &&
                Objects.equals(hst, order.hst) &&
                Objects.equals(total, order.total) &&
                Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, itemsSubtotal, shippingAndHandling, environmentalHandlingFee, totalBeforeTax, hst, total, items);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", itemsSubtotal=" + itemsSubtotal +
                ", shippingAndHandling=" + shippingAndHandling +
                ", environmentalHandlingFee=" + environmentalHandlingFee +
                ", totalBeforeTax=" + totalBeforeTax +
                ", hst=" + hst +
                ", total=" + total +
                ", items='" + items + '\'' +
                '}';
    }

}
