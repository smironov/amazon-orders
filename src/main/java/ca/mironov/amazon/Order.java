package ca.mironov.amazon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Order {

    private final String id;
    private final LocalDate date;
    private final BigDecimal totalBeforeTax;
    private final BigDecimal shippingAndHandling;
    private final BigDecimal hst;
    private final BigDecimal total;

    public Order(String id, LocalDate date, BigDecimal totalBeforeTax, BigDecimal shippingAndHandling, BigDecimal hst, BigDecimal total) {
        this.id = id;
        this.date = date;
        this.totalBeforeTax = totalBeforeTax;
        this.shippingAndHandling = shippingAndHandling;
        this.hst = hst;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
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
                Objects.equals(date, order.date) &&
                Objects.equals(totalBeforeTax, order.totalBeforeTax) &&
                Objects.equals(shippingAndHandling, order.shippingAndHandling) &&
                Objects.equals(hst, order.hst) &&
                Objects.equals(total, order.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, totalBeforeTax, shippingAndHandling, hst, total);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", totalBeforeTax=" + totalBeforeTax +
                ", shippingAndHandling=" + shippingAndHandling +
                ", hst=" + hst +
                ", total=" + total +
                '}';
    }

}
