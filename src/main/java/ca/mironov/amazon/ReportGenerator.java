package ca.mironov.amazon;

import java.io.IOException;
import java.util.List;

interface ReportGenerator {

    void generate(String reportName, List<Order> orders) throws IOException;

}
