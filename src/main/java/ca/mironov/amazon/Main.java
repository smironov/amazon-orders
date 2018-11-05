package ca.mironov.amazon;

import org.apache.commons.cli.*;
import org.slf4j.*;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final Option INPUT_DIR_OPTION = new Option("i", "input-dir", true, "Input directory");
    private static final Option OUTPUT_DIR_OPTION = new Option("o", "output-dir", true, "Output directory");

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
            logger.info("{}: saved {} orders", name, orders.size());
        }
    }

    public static void main(String[] args) throws ParseException {
        CommandLineParser commandLineParser = new DefaultParser();
        Options options = new Options();
        options.addOption(INPUT_DIR_OPTION);
        options.addOption(OUTPUT_DIR_OPTION);
        CommandLine commandLine = commandLineParser.parse(options, args);
        String inputDir = Objects.requireNonNull(
                commandLine.getOptionValue(INPUT_DIR_OPTION.getOpt()), "input directory is not specified");
        String outputDir = Objects.requireNonNull(
                commandLine.getOptionValue(OUTPUT_DIR_OPTION.getOpt()), "output directory is not specified");
        Path ordersDir = Path.of(inputDir);
        Map<String, OrdersFindService> ordersFindServiceMap = Map.of(
                "Books", new DirectoryOrdersFindService(ordersDir.resolve("Books")),
                "Cars", new DirectoryOrdersFindService(ordersDir.resolve("Cars")),
                "Computers", new DirectoryOrdersFindService(ordersDir.resolve("Computers")),
                "Office Expenses", new DirectoryOrdersFindService(ordersDir.resolve("Office Expenses")),
                "Supplies", new DirectoryOrdersFindService(ordersDir.resolve("Supplies"))
        );
        OrderParser orderParser = new KeyPDFOrderParser();
        ReportGenerator reportGenerator = new CsvReportGenerator(Path.of(outputDir));
        new Main(ordersFindServiceMap, orderParser, reportGenerator).run();
    }

}
