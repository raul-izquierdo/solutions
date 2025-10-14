package es.uniovi.raul.solutions.github;

/**
 * Represents a team in the organization.
 * Each team has a display name, a slug (unique identifier)
 *
 * @param displayName the display name of the team
 * @param slug the unique identifier (slug) of the team
 */
public record Team(String displayName, String slug) {

    public Team {

        if (displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("Display name cannot be null or blank.");

        if (slug == null || slug.isBlank())
            throw new IllegalArgumentException("Slug cannot be null or blank.");
    }
}
