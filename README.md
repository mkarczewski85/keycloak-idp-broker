# B2B IdP Broker - Keycloak Custom SPI

**One Realm To Rule Them All**: A sophisticated B2B authentication solution that enables email-based IdP routing without exposing partner identity providers to end users.

## Overview

This repository contains the Keycloak components for implementing a seamless B2B identity provider broker. The solution uses a custom SPI authenticator to collect user email addresses and automatically route them to their organization's IdP based on email domain mapping.

### Key Features

- **Email-based IdP routing**: Users only need to enter their corporate email address
- **Transparent federation**: No visible IdP selection screens for end users
- **Custom authentication flow**: Seamless integration with Keycloak's native federation
- **Database-driven mapping**: Flexible partner onboarding through configuration
- **Custom branded themes**: Professional UI matching corporate guidelines

## Architecture Components

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   IdP Broker    │◄──►│   PostgreSQL     │◄──►│ Partner IdP     │
│   (Keycloak)    │    │   Database       │    │ (Demo Keycloak) │
│                 │    │                  │    │                 │
│ • Custom SPI    │    │ • Keycloak DB    │    │ • OIDC Client   │
│ • Email Form    │    │ • Domain Mapping │    │ • Test Users    │
│ • Federation    │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Repository Contents

### Configuration Files
- `realms/broker/realm-broker.json` - Main broker realm configuration
- `realms/idp/realm-idp.json` - Demo partner IdP realm configuration
- `docker-compose.yml` - Complete infrastructure setup
- `init.sql` - Database schema and initial data

### Custom Implementation
- `custom-spi/` - Maven project with custom authenticator implementation
- `themes/b2b-custom/` - Custom Keycloak theme with email collection form

### Database Schema
```sql
CREATE TABLE IF NOT EXISTS domain_to_idp (
     email_domain TEXT PRIMARY KEY,
     idp_alias TEXT NOT NULL,
     enabled BOOLEAN NOT NULL DEFAULT FALSE
);
```

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 11+ and Maven 3.6+
- Git

### Installation Steps

1. **Clone the repository**
```bash
git clone <repository-url>
cd keycloak-b2b-idp-broker
```

2. **Build the custom SPI**
```bash
cd custom-spi
mvn clean package
cd ..
```

3. **Start the infrastructure**
```bash
docker-compose up
```

4. **Wait for services to initialize**
   The setup includes:
- PostgreSQL database (port 5432)
- IdP Broker Keycloak (port 8080)
- Partner IdP Keycloak (port 8081)

### Initial Configuration

**Database Setup:**
The `init.sql` script automatically creates the `domain_to_idp` table with sample data:
```sql
INSERT INTO domain_to_idp (email_domain, idp_alias, enabled)
VALUES ('partner.com', 'external-idp', TRUE)
    ON CONFLICT (email_domain) DO NOTHING;
```

**Realm Import:**
Both Keycloak instances automatically import their respective realm configurations on startup.

## Configuration Details

### Custom SPI Configuration

The custom authenticator is configured in the broker realm's authentication flow:

1. **Browser Flow Modification:**
    - Add "IdP Redirect Authenticator" as the first step
    - Configure as REQUIRED
    - Follow with standard "Identity Provider Redirector"

2. **Theme Configuration:**
    - Set realm theme to "b2b-custom"
    - Custom template: `email-form.ftl`
    - Branded styling and messaging

## Development

### Building the SPI

The custom SPI project uses standard Maven structure:

```
custom-spi/
├── src/main/java/
│   └── dev/karczewski/keycloak/auth/
│       ├── IdpRedirectAuthenticator.java
│       └── IdpRedirectAuthenticatorFactory.java
├── src/main/resources/
│   └── META-INF/services/
│       └── org.keycloak.authentication.AuthenticatorFactory
└── pom.xml
```

**Build commands:**
```bash
# Clean build
mvn clean package

# Skip tests (faster for development)
mvn clean package -DskipTests

# Install to local repository
mvn clean install
```

### Theme Development

The custom theme extends Keycloak's base theme:

```
themes/b2b-custom/
├── login/
│   ├── email-form.ftl
│   └── error.ftl
├── theme.properties
```

**Key template variables:**
- `${url.loginAction}` - Form submission URL
- `${message.summary}` - Error messages
- `${login.username!''}` - Pre-filled email value

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


---

**Note**: This is a demonstration setup. For production use, ensure proper security configuration, monitoring, and backup procedures are in place.