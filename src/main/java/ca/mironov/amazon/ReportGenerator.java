package ca.mironov.amazon;

import java.util.List;

public interface ReportGenerator {

    void generate(String reportName, List<Order> orders);

}
