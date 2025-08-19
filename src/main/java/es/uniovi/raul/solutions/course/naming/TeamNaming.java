package es.uniovi.raul.solutions.course.naming;

import static es.uniovi.raul.solutions.debug.Debug.*;

/**
 * Strategy for naming teams based on their group.
 */

// There is no need for a proper Strategy Pattern
public final class TeamNaming {

    private static final String PREFIX = "group ";

    public static String toTeam(String group) {
        notNull(group, "group");

        return PREFIX + group;
    }

    public static String toGroup(String teamName) {
        notNull(teamName, "teamName");

        if (!isGroupTeam(teamName))
            throw new IllegalArgumentException(
                    "Team name does not correspond to a group team: it should start with '" + PREFIX + "'");

        return teamName.substring(PREFIX.length());
    }

    public static boolean isGroupTeam(String team) {
        notNull(team, "team");

        return team.startsWith(PREFIX) && !team.substring(PREFIX.length()).isBlank();
    }

}
