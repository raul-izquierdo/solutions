# Solutions

Easily show or hide GitHub Classroom assignment solutions by granting or revoking team read access to solution repositories—often with just a single confirmation, thanks to automatic group and solution detection.

> **Note:** This tool is part of a broader toolkit for managing classes with GitHub Classroom. For an overview and to see how this fits in, check out the [main repository](https://github.com/raul-izquierdo/classroom-tools).

## See it in Action

The standout feature is automatic group and solution detection:
- Instantly identifies which group is currently in session using the schedule CSV and your system time.
- Selects the next solution repository for that group.

Just walk into class, run the tool, press `y`, and the right solution is unlocked for the right group.

Example `schedule.csv`:
```csv
G1,wednesday 08:00
G2,wednesday 09:00
G-english-1,wednesday 10:00
```

Running the tool:
```bash
$ java -jar solutions.jar -s schedule.csv

Connecting with GitHub... done.

(now: 'wednesday' 08:15) Proceed to show 'factorial-solution' to group 'G1'? (y/N): y
Access granted.
```

> **NOTE**. For this functionality to work, some naming conventions must be followed. See [Naming Rules](#naming-rules) for details.

If an automatic detection is not possible, you’ll get an interactive picker:

```bash
Choose the group:
(type to filter or use arrows ↑/↓): G1
> G1
  G2
  G-english-1

Choose the solution:
(type to filter or use arrows ↑/↓):
  linked-list-solution [accessible]
> factorial-solution [hidden]

Grant access? (y/N): y
Access granted.
```

When you select a solution manually, its access is toggled (granted or revoked) immediately.

## Usage

The JAR can be downloaded from the [releases page](https://github.com/raul-izquierdo/solutions/releases).

```bash
java -jar solutions.jar [-s schedule.csv] [-o <organization>] [-t <token>]
```

| Option              | Description                                             |
|---------------------|---------------------------------------------------------|
| `-s <schedule.csv>` | CSV file with the group schedule (default: `schedule.csv`). See [Schedule File Format](#schedule-file-format) for details. |
| `-o <organization>` | GitHub organization name                               |
| `-t <token>`        | GitHub API access token. For more details, see [Obtaining the GitHub token](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-github-token).                                |

If you don’t provide `-o` or `-t`, the tool will look for `GITHUB_ORG` and `GITHUB_TOKEN` in a `.env` file in your working directory:
```dotenv
GITHUB_ORG=<your-org>
GITHUB_TOKEN=<token>
```

## Schedule File Format

Your CSV should have one line per group, like this:

```csv
<groupLabel>, <weekday>, <start-time>[, <duration>]
```

- **groupLabel:** Any text (the group’s name)
- **weekday:** monday, tuesday, wednesday, thursday, friday, saturday, or sunday
- **start-time:** `H:mm`, `HH:mm`, or `H` (e.g., `8:15`, `09:30`, `8`), between 08:00 and 21:00
- **duration** (optional):
  - Defaults to 2 hours if omitted
  - Accepts:
    - hours: `2` or `2h`
    - minutes: `120m`

Notes:
- The CSV file should not include a header row.
- If a group appears more than once, the last entry is used.

Example (all 2 hours):
```csv
G4, thursday, 12:00
G3, wednesday, 8:15, 2
G1, monday, 10:00, 2h
G2, tuesday, 09:30, 120m
```

> NOTE: At the moment, more than one timeslot per group (multiple classes per week) is not supported. It wouldn’t be hard to add; if requested, it can be considered.

## Naming Rules

To help the tool recognize which teams are groups and which repositories are solutions, follow these conventions:

### Team Names for Groups

A team is treated as a group if its display name starts with `group ` (note the space) followed by the group name.

| Group      | Team name         |
|------------|-------------------|
| 01         | group 01          |
| i02        | group i02         |
| lab1       | group lab1        |
| 01_english | group 01_english  |

### Repository Names for Solutions

1. A repository is considered a solution if its name ends with `solution`.

    | Assignment name   | Solution repository name   |
    |-------------------|---------------------------|
    | factorial         | factorial-solution        |
    | listas_enlazadas  | linked-list-solution      |
    | pila              | stack-solution            |
    | strategy pattern  | strategy-pattern-solution |

2. For automatic solution selection, name your solution repositories so that sorting them alphabetically matches the order you want to reveal them. A simple way is to prefix them with the class number, e.g., `01-factorial-solution`.

Summary:
- Start the repository name with the class number.
- End it with `solution`.

Examples (note that the separators doesn’t matter):
`01-factorial-solution`, `02_listas_enlazadas_solution`, `03-pila-solution`, `04.strategy-pattern-solution`.

## License

See `LICENSE`.
Copyright (c) 2025 Raul Izquierdo Castanedo
