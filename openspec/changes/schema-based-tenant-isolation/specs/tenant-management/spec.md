## ADDED Requirements

### Requirement: Tenant Creation
The system SHALL allow creating new tenants with unique identifiers and names.

#### Scenario: Successful Tenant Creation
- **WHEN** a request is made to create a tenant with valid name
- **THEN** the system SHALL create a new tenant record with a unique ID
- **THEN** the system SHALL return the tenant ID and name in the response

### Requirement: Tenant Query
The system SHALL allow querying tenants by ID or name.

#### Scenario: Query Tenant by ID
- **WHEN** a request is made to get a tenant by valid ID
- **THEN** the system SHALL return the tenant details

#### Scenario: Query Tenant by Name
- **WHEN** a request is made to get a tenant by valid name
- **THEN** the system SHALL return the tenant details

### Requirement: Tenant Update
The system SHALL allow updating tenant information.

#### Scenario: Update Tenant Name
- **WHEN** a request is made to update a tenant's name with valid ID
- **THEN** the system SHALL update the tenant's name
- **THEN** the system SHALL return the updated tenant details

### Requirement: Tenant Deletion
The system SHALL allow deleting tenants by ID.

#### Scenario: Delete Tenant
- **WHEN** a request is made to delete a tenant by valid ID
- **THEN** the system SHALL mark the tenant as deleted
- **THEN** the system SHALL return a success message