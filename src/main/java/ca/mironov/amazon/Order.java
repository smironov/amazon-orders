package ca.mironov.amazon;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {

    private final String id;
    private final BigDecimal totalBeforeTax;
    private final BigDecimal shippingAndHandling;
    private final BigDecimal hst;
    private final BigDecimal total;

    public Order(String id, BigDecimal totalBeforeTax, BigDecimal shippingAndHandling, BigDecimal hst, BigDecimal total) {
        this.id = id;
        this.totalBeforeTax = totalBeforeTax;
        this.shippingAndHandling = shippingAndHandling;
        this.hst = hst;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getTotalBeforeTax() {
        return totalBeforeTax;
    }

    public BigDecimal getShippingAndHandling() {
        return shippingAndHandling;
    }

    public BigDecimal getHst() {
        return hst;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(totalBeforeTax, order.totalBeforeTax) &&
                Objects.equals(shippingAndHandling, order.shippingAndHandling) &&
                Objects.equals(hst, order.hst) &&
                Objects.equals(total, order.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, totalBeforeTax, shippingAndHandling, hst, total);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", totalBeforeTax=" + totalBeforeTax +
                ", shippingAndHandling=" + shippingAndHandling +
                ", hst=" + hst +
                ", total=" + total +
                '}';
    }

}
