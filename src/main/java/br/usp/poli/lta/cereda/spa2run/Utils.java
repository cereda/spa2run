/**
* ------------------------------------------------------
*    Laboratório de Linguagens e Técnicas Adaptativas
*       Escola Politécnica, Universidade São Paulo
* ------------------------------------------------------
* 
* This program is free software: you can redistribute it
* and/or modify  it under the  terms of the  GNU General
* Public  License  as  published by  the  Free  Software
* Foundation, either  version 3  of the License,  or (at
* your option) any later version.
* 
* This program is  distributed in the hope  that it will
* be useful, but WITHOUT  ANY WARRANTY; without even the
* implied warranty  of MERCHANTABILITY or FITNESS  FOR A
* PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
* 
**/
package br.usp.poli.lta.cereda.spa2run;

import br.usp.poli.lta.cereda.aa.examples.ExampleState;
import br.usp.poli.lta.cereda.aa.examples.ExampleSymbol;
import br.usp.poli.lta.cereda.aa.execution.AdaptiveAutomaton;
import br.usp.poli.lta.cereda.aa.model.Action;
import br.usp.poli.lta.cereda.aa.model.State;
import br.usp.poli.lta.cereda.aa.model.Submachine;
import br.usp.poli.lta.cereda.aa.model.Symbol;
import br.usp.poli.lta.cereda.aa.model.Transition;
import br.usp.poli.lta.cereda.aa.model.sets.Mapping;
import br.usp.poli.lta.cereda.spa2run.importer.Spec;
import br.usp.poli.lta.cereda.spa2run.metrics.Metric;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Utils {

    private static List<Metric> instruments = new ArrayList<>();
    private static Map<String, Double> calculations = new HashMap<>();

    public static Options getOptions() {
        Options options = new Options();
        Option instrumentation = new Option("i", "instrumentation");
        instrumentation.setArgs(Option.UNLIMITED_VALUES);
        instrumentation.setDescription("instrumentation files");
        options.addOption(instrumentation);
        return options;
    }

    public static List<Spec> fromFilesToSpecs(String[] values)
            throws Exception {
        if (values.length == 0) {
            throw new Exception("The tool requires at least one automaton spec."
                    + " Make sure to provide such file and try again.");
        }

        List<Spec> specs = new ArrayList<>();
        for (String value : values) {
            File file = new File(value);
            if (!file.exists()) {
                throw new Exception("The automaton spec '" + file.getName() +
                        "' does not exist. Make sure to provide the correct"
                        + " file location and try again.");
            }
            Yaml yaml = new Yaml();
            specs.add(yaml.loadAs(new FileReader(file), Spec.class));
        }

        return specs;
    }

    public static void printBanner() {
        StringBuilder sb = new StringBuilder();
        sb.append("               ___               ").append('\n');
        sb.append(" ____ __  __ _|_  )_ _ _  _ _ _  ").append('\n');
        sb.append("(_-< '_ \\/ _` |/ /| '_| || | ' \\ ").append('\n');
        sb.append("/__/ .__/\\__,_/___|_|  \\_,_|_||_|").append('\n');
        sb.append("   |_|   ").append('\n');
        System.out.println(sb.toString());
    }

    public static AdaptiveAutomaton getAutomatonFromSpecs(List<Spec> specs) {
        AdaptiveAutomaton aa = new AdaptiveAutomaton() {
            @Override
            public void setup() {

                Action metrics = new Action("metrics") {
                    @Override
                    public void execute(Mapping transitions,
                            Transition transition, Object... parameters) {
                        int id = (int) parameters[0];
                        instruments.stream().forEach((metric) -> {
                            metric.getMapping().stream().filter((m) ->
                                    (m.getIdentifier() == id)).forEach((m) -> {
                                calculations.put(metric.getName(),
                                        calculations.get(metric.getName())
                                                + m.getValue());
                            });
                        });
                    }
                };
                actions.add(metrics);

                setMainSubmachine(specs.get(0).getName());

                for (Spec spec : specs) {

                    Set<State> accepting = new HashSet<>();
                    Set<State> all = new HashSet<>();

                    all.add(new ExampleState(spec.getInitial()
                            + spec.getName()));

                    spec.getAccepting().stream().map((i) ->
                            new ExampleState(i + spec.getName())).map((es) -> {
                        accepting.add(es);
                        return es;
                    }).forEach((es) -> {
                        all.add(es);
                    });

                    spec.getTransitions().stream().map((t) -> {
                        Transition transition = new Transition();
                        State from = new ExampleState(t.getFrom()
                                + spec.getName());
                        State to = new ExampleState(t.getTo() + spec.getName());
                        all.add(from);
                        all.add(to);
                        transition.setSourceState(from);
                        transition.setTargetState(to);
                        if (t.getSymbol() != null) {
                            String s = t.getSymbol();
                            if (s.endsWith(" (call)")) {
                                transition.setSubmachineCall(s.substring(0,
                                        s.length() - 7));
                            } else {
                                Symbol symbol = new ExampleSymbol(s);
                                transition.setSymbol(symbol);
                            }
                        }
                        if (t.getIdentifier() != 0) {
                            transition.setPriorActionCall("metrics");
                            transition.setPriorActionArguments(new
                                    Object[]{t.getIdentifier()});
                        }
                        return transition;
                    }).forEach((transition) -> {
                        transitions.add(transition);
                    });

                    Submachine sm = new Submachine(spec.getName(), all,
                            new ExampleState(spec.getInitial() +
                                    spec.getName()), accepting);
                    submachines.add(sm);

                }

            }
        };

        return aa;
    }

    public static List<Symbol> toSymbols(String s) {
        List<Symbol> result = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            result.add(new ExampleSymbol("" + s.charAt(i)));
        }
        return result;
    }

    public static boolean detectEpsilon(AdaptiveAutomaton a) {
        return a.getTransitions().getTransitions().stream().anyMatch((t)
                -> (t.isEpsilonTransition()));
    }

    static List<Metric> fromFilesToMetrics(CommandLine line) throws Exception {
        List<Metric> result = new ArrayList<>();
        if (line.hasOption("i")) {
            for (String s : line.getOptionValues("i")) {
                File file = new File(s);
                if (!file.exists()) {
                    throw new Exception("file has to exist!");
                }
                Yaml yaml = new Yaml();
                result.add(yaml.loadAs(new FileReader(file), Metric.class));
            }
        }
        return result;
    }

    public static void setMetrics(List<Metric> metrics) {
        Utils.instruments = metrics;
        Utils.calculations = new HashMap<>();
    }

    public static void resetCalculations() {
        instruments.stream().forEach((m) -> {
            calculations.put(m.getName(), 0.0);
        });
    }

    public static String prettyPrintMetrics() {
        String result = "";
        for (Metric m : instruments) {
            result = result.concat(String.format("%s: %1.2f ",
                    m.getName(), calculations.get(m.getName())));
        }
        return result.trim();
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("spa2run <specs> -i <metrics>", getOptions());
        System.exit(0);
    }

    public static void printException(Exception exception) {
        System.out.println(StringUtils.repeat("-", 70));
        System.out.println(StringUtils.center("An exception was thrown".
                toUpperCase(), 70));
        System.out.println(StringUtils.repeat("-", 70));
        System.out.println(WordUtils.wrap(exception.getMessage(),
                70, "\n", true));
        System.out.println(StringUtils.repeat("-", 70));
        System.exit(0);
    }

}
