# SM Loader

_Fetches and processes cases from the openE platform and seamlessly integrates them into the Support Management system. Provides automated case migration with error handling and notification capabilities._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Sundsvallskommun/api-service-sm-loader.git
   cd api-service-sm-loader
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **Messaging**
  - **Purpose:** Used to send messages if something goes wrong with loading cases.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Open-e Platform**
  - **Purpose:** This service retrieves cases from openE platform.
  - **Repository:** [Open-ePlatform](https://github.com/Open-ePlatform/Open-ePlatform)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Party**
  - **Purpose:** Used for translating between party id and legal id.
  - **Repository:** [https://github.com/Sundsvallskommun/api-service-party](https://github.com/Sundsvallskommun/api-service-party)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
  - **Configuration**: See [Key Configuration Parameters](#key-configuration-parameters) for configuration regarding the Party service.
- **SupportManagement**
  - **Purpose:** The target service for the cases read from openE.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-support-management)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/2281/jobs/caseexporter
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **External Service URLs:**

  ```yaml
  integration:
    open-e:
        external:
          username: username
          password: password
          url: http://open-e-external.url
        internal:
          username: username
          password: password
          url: http:/open-e-internal.url
        external-soap:
          username: username
          password: password
          url: http://open-e-external-soap.url
        internal-soap:
          username: username
          password: password
          url: http://open-e-internal-soap.url
    support-management:
        url: http://support-management.url
        token-uri: http://token.url
        client-id: the-client-id
        client-secret: the-client-secret
    party:
        url: http://party.url
        client-id: the-client-id
        client-secret: the-client-secret
    messaging:
        url: http://messaging.url
        token-uri: http://token.url
        client-id: the-client-id
        client-secret: the-client-secret
        channel: channel
        token: token
        mailRecipient: mailRecipient
  ```
- **OpenE metadata configuration:**

  ```yaml
  lamna-synpunkt:
    family-id: 123
    priority: LOW
    category: CATEGORY
    type: TYPE
  sundsvallsforslaget:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
  ersattare-chef:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
  ny-behorighet:
    family-id: 123
    priority: MEDIUM
    category: SYSTEM_MANAGEMENT
    type: TYPE
  lonevaxling-pension:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
  kontakt-lon-pension:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
  tjugofem-ar-pa-jobbet:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
    labels:
      - LABEL.1
      - LABEL.2
  anmal-sjukfranvaro:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
    labels:
      - LABEL.1
      - LABEL.2
  arbetsgivarintyg:
    family-id: 123
    priority: MEDIUM
    category: SALARY
    type: TYPE
    labels:
      - LABEL.1
      - LABEL.2
  foretradesratt-ateranstallning:
    family-id: 123
    priority: MEDIUM
    category: CATEGORY
    type: TYPE
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-sm-loader&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-sm-loader)

---

© 2024 Sundsvalls kommun
