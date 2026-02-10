## ADDED Requirements

### Requirement: Schema Creation
The system SHALL automatically create a database Schema for each new tenant.

#### Scenario: Create Schema for New Tenant
- **WHEN** a new tenant is created
- **THEN** the system SHALL create a new database Schema with name `tenant_{tenant_id}`
- **THEN** the system SHALL return the Schema creation status

### Requirement: Schema Initialization
The system SHALL initialize the created Schema with necessary table structures.

#### Scenario: Initialize Schema
- **WHEN** a new Schema is created
- **THEN** the system SHALL execute initialization SQL scripts
- **THEN** the system SHALL create all necessary tables in the Schema
- **THEN** the system SHALL return the initialization status

### Requirement: Schema Existence Check
The system SHALL check if a Schema exists before creating it.

#### Scenario: Check Schema Existence
- **WHEN** a request is made to create a Schema
- **THEN** the system SHALL check if the Schema already exists
- **THEN** the system SHALL only create the Schema if it doesn't exist

### Requirement: Schema Deletion
The system SHALL allow deleting a tenant's Schema when the tenant is deleted.

#### Scenario: Delete Schema
- **WHEN** a tenant is deleted
- **THEN** the system SHALL delete the corresponding Schema
- **THEN** the system SHALL return the deletion status