package ca.mironov.amazon;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class Main {

    private static final Option INPUT_DIR_OPTION = new Option("i", "input-dir", true, "Input directory");
    private static final Option OUTPUT_DIR_OPTION = new Option("o", "output-dir", true, "Output directory");

    private final DirectoryOrdersFindService ordersFindService;
    private final KeyPDFOrderParser orderParser;
    private final ReportGenerator reportGenerator;

    private Main(DirectoryOrdersFindService ordersFindService, KeyPDFOrderParser orderParser, ReportGenerator reportGenerator) {
        this.ordersFindService = ordersFindService;
        this.orderParser = orderParser;
        this.reportGenerator = reportGenerator;
    }

    private int run(String... categories) throws IOException {
        int ordersTotal = 0;
        for (String category : categories) {
            List<Order> orders = ordersFindService.findOrderFiles(category)
                    .stream()
                    .map(file -> orderParser.parse(category, file))
                    .sorted(Comparator.comparing(Order::date))
                    .toList();
            reportGenerator.generate(category, orders);
            log.info("{}: saved {} orders", category, orders.size());
            ordersTotal += orders.size();
        }
        return ordersTotal;
    }

    public static void main(String[] args) {
        //noinspection OverlyBroadCatchBlock
        try {
            Options options = new Options();
            options.addOption(INPUT_DIR_OPTION);
            options.addOption(OUTPUT_DIR_OPTION);
            CommandLine commandLine = new DefaultParser().parse(options, args);
            String inputDir = Objects.requireNonNull(commandLine.getOptionValue(INPUT_DIR_OPTION.getOpt()), "input directory is not specified");
            String outputDir = Objects.requireNonNull(commandLine.getOptionValue(OUTPUT_DIR_OPTION.getOpt()), "output directory is not specified");

            DirectoryOrdersFindService ordersFindService = new DirectoryOrdersFindService(Path.of(inputDir));
            ReportGenerator reportGenerator = new CsvReportGenerator(Path.of(outputDir));
            int ordersTotal = new Main(ordersFindService, new KeyPDFOrderParser(), reportGenerator)
                    .run("Computers", /*"Furniture", */"Tools Software Books", "Supplies");
            log.info("Reports saved to {}, {} orders", outputDir, ordersTotal);
        } catch (Exception e) {
            log.error("FATAL ERROR", e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Main.class);

}
