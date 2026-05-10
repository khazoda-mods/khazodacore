# KhazodaCore

KhazodaCore is a required multiloader library mod for Khazoda's Minecraft mods on 26.1.x and later.

It provides shared utilities for:

- multiloader static registration helpers
- simple config .properties files
- server-to-client config synchronization

## Publishing

KhazodaCore publishes Maven artifacts to a local staging repository first:

```powershell
.\gradlew.bat :common:publish :fabric:publish :neoforge:publish --console plain
```

The staged Maven repository is written to:

```text
build/maven-repo
```

GitHub Actions can publish that staging repository to the public maven:

```text
https://maven.khazoda.com/releases
```

Required repository secrets:

- `KHAZODA_MAVEN_HOST`
- `KHAZODA_MAVEN_USER`
- `KHAZODA_MAVEN_SSH_KEY`
- `KHAZODA_MAVEN_PORT` optional, defaults to `22`
