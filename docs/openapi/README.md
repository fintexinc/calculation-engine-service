# The published API contract

`portfolio-calculation-engine-api-generated.yaml` is the springdoc document this service serves at
`/api-docs`, checked in so the contract can be read, diffed and reviewed without running anything.

## It cannot go stale

`OpenApiContractE2ETest` boots the service, fetches the document and compares it with the file. A
change to a path, a payload property or a schema that nobody meant to publish fails the build instead
of being noticed months later — which is what happened to the Market Investment Catalogue's contract,
where twenty documented endpoints all answered `404` for three months because no test or build step
ever looked at it.

After deliberately changing an endpoint or a payload:

```bash
./gradlew :bootstrap:test --tests '*OpenApiContractE2ETest' -Dopenapi.update=true
```

Then review the diff. It is the part of the change that reaches clients, so it belongs in the pull
request rather than in someone's memory.

## Naming

The contract follows the Tangerine API design guidelines. When adding or changing an endpoint:

| Element                 | Convention           | Example                                            |
|-------------------------|----------------------|----------------------------------------------------|
| Base path               | `/api/v<major>`      | `/api/v1`                                          |
| Path segments           | lowercase kebab-case | `/portfolio/calculations`                          |
| Path template variables | lowercase kebab-case | `/{metric-name}`                                   |
| Query parameters        | `lower_snake_case`   | none today                                         |
| Payload data properties | `camelCase`          | `dataProviders`, `timeIntervalPeriods`             |
| Payload object (schema) | `PascalCase`         | `CompositeCalculationRequest`, `PeriodCommand`     |
| HTTP headers            | hyphenated           | `X-Request-ID`                                     |
| Resource names          | domain plural nouns  | `calculations` under `portfolio`, no verbs         |

Payload properties are camelCase, which is what every Tangerine service serves today: the guidelines
reserve `lower_snake_case` for query parameters, and nothing in the estate spells a body that way.
So no naming strategy is configured at all — a DTO's Java field name is its wire name, and the
primary `ObjectMapper` serves the API as it serves everything else.

`portfolio` stays a path segment of its own rather than being folded into `portfolio-calculations`:
it is the root entity that other resources hang off, and calculations are one of them.

## Still to come

The Tangerine template also asks for a curated document beside the export — paths grouped by area,
request bodies and error payloads factored into named components. The Market Investment Catalogue
generates its curated copy from its export (`docs/openapi/tools/` in that repository) rather than
maintaining it by hand, and the same generator should be pointed at this export once it exists.
