# coverage

To get the level of covergae for the rest api, run: mvn verify then mvn jacoco:report

# rest-api

Spring boot rest api for querying the database

### Accessing the swagger dashboard

To access the swagger dashboard, it must be enabled using the `ENABLE_SPRING_DOCS` environment variable.
You can access the dashboard at `{url}/swagger-ui/index.html`

### Required Environment variables
| Environment Variable     | Description                                                                                                          |
|--------------------------|----------------------------------------------------------------------------------------------------------------------| 
| POSTGRES_HOST            | The hostname of the postgres database                                                                                |
| POSTGRES_PORT            | The port of the postgres database                                                                                    |
| POSTGRES_DB_NAME         | The name of the db for the postgres database                                                                         |
| POSTGRES_USERNAME        | Postgres username for access. Should contain write permissions                                                       |
| POSTGRES_PASSWORD        | Postgres password for authentication                                                                                 |
| MONGO_HOST               | The hostname of the mongodb database                                                                                 |
| MONGO_PORT               | The port of the mongodb database                                                                                     |
| MONGO_USERNAME           | The username for access. Should container write permissions                                                          |
| MONGO_PASSWORD           | Mongodb password for authentication                                                                                  |
| MONGO_DB_NAME            | Mongodb Database name                                                                                                |
| MONGO_AUTH_DB            | **Optional** Authentication database name for mongodb. By default is "admin"                                         |
| REST_API_PORT            | The port to host the rest API. Default: 8080                                                                         |
| ENABLE_SPRING_DOCS       | Enable Spring docs endpoints. Default: false. Endpoints are available at `/swagger-ui/index.html` and `/v3/api-docs` |
| ALLOWED_ORIGINS_PATTERNS | Allowed origin patterns for CORS                                                                                     |

### Configuration Options
| Configuration      | Type   | Description                                                                              |
|--------------------|--------|------------------------------------------------------------------------------------------|
| api.dashboard-link | string | The link to the read page for any given report. (e.g `http://example.com/reports/read/`) |
