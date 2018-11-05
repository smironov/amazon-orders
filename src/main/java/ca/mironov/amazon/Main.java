package ca.mironov.amazon;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private final Map<String, OrdersFindService> ordersFindServiceMap;
    private final OrderParser orderParser;
    private final ReportGenerator reportGenerator;

    private Main(Map<String, OrdersFindService> ordersFindServiceMap, OrderParser orderParser,
                 ReportGenerator reportGenerator) {
        this.ordersFindServiceMap = ordersFindServiceMap;
        this.orderParser = orderParser;
        this.reportGenerator = reportGenerator;
    }

    private void run() {
        for (Map.Entry<String, OrdersFindService> entry : ordersFindServiceMap.entrySet()) {
            String name = entry.getKey();
            OrdersFindService ordersFindService = entry.getValue();
            List<Order> orders = ordersFindService.findOrderFiles().stream()
                    .map(orderParser::parse)
                    .collect(Collectors.toList());
            reportGenerator.generate(name, orders);
        }
    }

    public static void main(String[] args) {
        Path ordersDir = Path.of("orders");
        Map<String, OrdersFindService> ordersFindServiceMap = Map.of(
                "computers", new DirectoryOrdersFindService(ordersDir.resolve("computers")),
                "books", new DirectoryOrdersFindService(ordersDir.resolve("books"))
        );
        OrderParser orderParser = new PDFOrderParser();
        ReportGenerator reportGenerator = new CsvReportGenerator(Path.of("reports"));
        new Main(ordersFindServiceMap, orderParser, reportGenerator).run();
    }

}
