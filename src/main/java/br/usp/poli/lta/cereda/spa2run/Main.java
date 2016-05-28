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

import br.usp.poli.lta.cereda.aa.execution.AdaptiveAutomaton;
import br.usp.poli.lta.cereda.spa2run.importer.Spec;
import br.usp.poli.lta.cereda.spa2run.metrics.Metric;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {

        Utils.printBanner();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(Utils.getOptions(), args);
            List<Spec> specs = Utils.fromFilesToSpecs(line.getArgs());
            List<Metric> metrics = Utils.fromFilesToMetrics(line);
            Utils.setMetrics(metrics);
            Utils.resetCalculations();
            AdaptiveAutomaton automaton = Utils.getAutomatonFromSpecs(specs);

            System.out.println("SPA generated successfully:");
            System.out.println("- " + specs.size() + " submachine(s) found.");
            if (!Utils.detectEpsilon(automaton)) {
                System.out.println("- No empty transitions.");
            }
            if (!metrics.isEmpty()) {
                System.out.println("- " + metrics.size() + " metric(s) found.");
            }

            System.out.println("\nStarting shell, please wait...\n"
                    + "(press CTRL+C or type `:quit'\n"
                    + "to exit the application)\n");

            String query = "";
            Scanner scanner = new Scanner(System.in);
            String prompt = "[%d] query> ";
            String result = "[%d] result> ";
            int counter = 1;
            do {

                try {
                    String term = String.format(prompt, counter);
                    System.out.print(term);
                    query = scanner.nextLine().trim();
                    if (!query.equals(":quit")) {
                        boolean accept
                                = automaton.recognize(Utils.toSymbols(query));
                        String type = automaton.getRecognitionPaths()
                                .size() == 1 ? " (deterministic)"
                                        : " (nondeterministic)";
                        System.out.println(String.format(result, counter)
                                + accept + type);

                        if (!metrics.isEmpty()) {
                            System.out.println(StringUtils.repeat(" ",
                                    String.format("[%d] ", counter).length())
                                    + Utils.prettyPrintMetrics());
                        }

                        System.out.println();

                    }
                } catch (Exception exception) {
                    System.out.println();
                    Utils.printException(exception);
                    System.out.println();
                }

                counter++;
                Utils.resetCalculations();

            } while (!query.equals(":quit"));
            System.out.println("That's all folks!");

        } catch (ParseException nothandled) {
            Utils.printHelp();
        } catch (Exception exception) {
            Utils.printException(exception);
        }
    }

}
