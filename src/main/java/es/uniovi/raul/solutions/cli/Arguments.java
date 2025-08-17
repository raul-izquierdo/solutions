package es.uniovi.raul.solutions.cli;

import picocli.CommandLine.*;

// CHECKSTYLE:OFF

@Command(name = "solutions", version = "1.0.0", showDefaultValues = true, mixinStandardHelpOptions = true, usageHelpAutoWidth = true, description = Messages.DESCRIPTION, footer = Messages.CREDITS)
public class Arguments {

    @Option(names = "-t", description = "GitHub API access token. If not provided, it will try to read from the GITHUB_TOKEN environment variable or from a '.env' file.")
    public String token;

    @Option(names = "-o", description = "GitHub organization name. If not provided, it will try to read from the GITHUB_ORG environment variable or from a '.env' file.")
    public String organization;

    @Option(names = "-s", description = "The CSV file with the groups schedule")
    public String scheduleFile;
}

class Messages {
    static final String DESCRIPTION = """

            Hides/shows the solution of a GitHub Classroom assignment for a specific group of students.
            For more information, visit: https://github.com/raul-izquierdo/solutions
            """;

    static final String CREDITS = """

            Escuela de Ingeniería Informática, Universidad de Oviedo.
            Raúl Izquierdo Castanedo (raul@uniovi.es)
            """;

}
