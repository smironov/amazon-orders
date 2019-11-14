package ca.mironov.amazon;

import java.io.IOException;
import java.util.List;

public interface ReportGenerator {

    void generate(String reportName, List<? extends Order> orders) throws IOException;

}
