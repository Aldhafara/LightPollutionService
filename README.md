# LightPollutionService

![Build](https://github.com/Aldhafara/LightPollutionService/actions/workflows/ci.yml/badge.svg)

![License](https://img.shields.io/github/license/Aldhafara/LightPollutionService)

![Last Commit](https://img.shields.io/github/last-commit/Aldhafara/LightPollutionService)

LightPollutionService provides real-time sky darkness ratings and light pollution data for any location, empowering
astrophotographers to find the best stargazing spots.

## Table of Contents

- [Features](#features)
- [Configuration](#configuration)
- [How to Run](#how-to-run)
- [How to Run with Docker](#how-to-run-with-docker)
- [API Documentation (Swagger/OpenAPI)](#api-documentation-swaggeropenapi)
- [API - Endpoints](#api-endpoints)
- [API - Request Parameters](#api-request-parameters)
- [API Response Format](#api-response-format)
- [Caching](#caching)
- [Rate Limiting](#rate-limiting)
- [Error Handling](#error-handling)
- [Example Usage](#example-usage)
- [How to Test](#how-to-test)
- [Troubleshooting](#troubleshooting)
- [License](#license)
- [Data Source](#data-source)

## Features

Available:

- /status health-check endpoint (server status, uptime, timestamp)
- /darkness sky brightness rating for given coordinates

## Configuration

Before running the application, you need an `application.properties` file with your local configuration (paths, API keys, etc.).

1. Copy the example to create your own config:
```bash
   cp src/main/resources/example-application.properties src/main/resources/application.properties
```

2. Edit `src/main/resources/application.properties` and fill in the required values for your environment.

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/Aldhafara/LightPollutionService.git
```

2. Prepare your configuration:
```bash
   cp src/main/resources/example-application.properties src/main/resources/application.properties
```
(then edit as needed)

3. Start the application:

```bash
./mvnw spring-boot:run
```

4. By default, the application will be available at:

```
http://localhost:8080
```

## How to Run with Docker

1. Build the JAR:

```bash
./mvnw clean package
```

2. (Optional) Prepare your application.properties if you want to override the config.

3. Build the image:

```bash
docker build -t lightpollutionservice .
```

4. Run (with local `application.properties` mounted):

```bash
docker run -p 8080:8080 -v $(pwd)/src/main/resources/application.properties:/app/application.properties lightpollutionservice
```

5. (or, with Docker Compose)

```bash
docker compose up --build
```

Once running, the application will be available at:

```
http://localhost:8080
```

## API Documentation (Swagger/OpenAPI)

Once the application is running, interactive API documentation will be available at:

```
http://localhost:8080/swagger-ui.html
```

You can explore, test, and understand all endpoints directly from your browser.

## API Endpoints

| Endpoint  | Type | Description                                 |
|-----------|------|---------------------------------------------|
| /status   | GET  | Server status, uptime, timestamp            |
| /darkness | GET  | Sky brightness rating for given coordinates |

## API Request Parameters

| Parameter | Type  | Description        |
|-----------|-------|--------------------|
| latitude  | float | Location latitude  |
| longitude | float | Location longitude |

**Example requests:**

```
GET /status
GET /darkness?latitude=50.06143&longitude=19.93658
```

## API Response Format

### Example `/status` Response

```json
{
  "status": "UP",
  "uptime": 5025112,
  "uptimePretty": "1h 23m 45s",
  "timestamp": "2025-07-14T18:52:00Z",
  "ratelimitRequests":50,
  "ratelimitDurationSeconds":60
}
```

**Description of key fields:**

- `status` - server status
- `uptime` - server uptime in ms
- `uptimePretty` - server uptime in easy to read form
- `timestamp` - timestamp of request
- `ratelimitRequests` - maximum number of queries allowed in a given time window
- `ratelimitDurationSeconds` - Length of the time window (in seconds) for which the query limit is assumed

### Example `/darkness` Response

```json
{
  "latitude": 52.232222,
  "longitude": 21.008333,
  "relativeBrightness": 0.12
}
```

**Description of key fields:**

- `latitude`, `longitude` - query coordinates
- `relativeBrightness` - light intensity (lower = darker)

## Caching

- Lookup results are cached (planned: per location for 1 hour).
- First request may be slower (file read), subsequent ones are instant.

## Rate Limiting

This is a protection for the `/darkness` endpoint against excessive requests - the limit is set to 50 requests per minute per IP.

- Limit: 50 requests per minute per IP address (configurable in application.properties under the keys `ratelimit.requests` and `ratelimit.durationSeconds`).
- Restricted endpoint: `/darkness` (requires the @RateLimited annotation)
- Exceeding the limit: returns HTTP status 429 (Too Many Requests) along with a JSON error message.
- Mechanism: based on Spring AOP, the RateLimitAspect class controls the number of requests.
- Testing: integration tests verify that the endpoint properly returns 429 when the limit is exceeded.
If needed, the limit can be adjusted or extended to other endpoints by adding the @RateLimited annotation and configuring accordingly.

## Error Handling

Planned features. Not yet implemented.

- 503 - if data unavailable (e.g., file read error)
- 422 - invalid input parameters (latitude, longitude)
- All errors in clear JSON format:

```json
{
  "error": "Invalid parameter: latitude",
  "timestamp": "2025-07-14T18:52:00Z"
}
```

## Example Usage

```bash
curl "http://localhost:8080/darkness?latitude=50.06143&longitude=19.93658"
```

## How to Test

Run tests:

```bash
./mvnw test
```

## Troubleshooting

- If you see errors about missing configuration, make sure `src/main/resources/application.properties` exists and is correctly filled.
- For Docker users, you can mount your configuration file as a volume if not building it into the image directly.
- Example Error:  
  `Could not resolve placeholder...`  
  Make sure your application.properties contains all needed variables.

## License

MIT

## Data Source

This product was made utilizing VIIRS Nighttime Lights data produced by the Earth Observation Group, Payne Institute for
Public Policy, Colorado School of Mines.

### Data:

[EOG VIIRS Nighttime Lights - annual composites](https://eogdata.mines.edu/products/vnl/#annual_v2)

License: Creative Commons Attribution 4.0 (CC-BY 4.0)
