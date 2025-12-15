# ClinApp

Comprehensive desktop application for managing a psychology clinic's operations, including patient records, session tracking, payments, and accounting.

## About

Personal project developed to streamline administrative tasks for a private psychology practice. Currently in production use since October 2025.

## Features

### Patient Management
- Register patients with customizable session costs
- Three patient categories: regular, monthly payment, and diagnosis
- Track patient debt automatically
- Add session notes and relevant information

### Session Tracking
- Quick session registration with auto-populated costs
- Multiple session types: regular, couples, family, trauma, diagnosis
- Payment status tracking (pending/paid)
- Monthly view with filtering by month and year
- Smart patient search with type-ahead filtering

### Medical Reports
- Generate and track various documentation types
- Report categories: letters, diagnoses, proof of treatment, institutional communications
- Progress tracking: missing tests, pending, finished, delivered
- Payment management: advance payments, partial payments, full payment
- Cost tracking per report

### Payment Processing
- Record payments by date and amount
- Multiple payment methods: cash, bank transfer, etc.
- Payment concepts: sessions, monthly fees, reports, diagnoses
- Monthly payment overview with historical filtering

### Accounting
- Monthly revenue summary broken down by patient type
- Total outstanding debt tracking
- Detailed debt breakdown by patient
- Shows number of unpaid sessions and pending reports

### Automatic Backups
- Daily automatic saves
- Export to both CSV and XLSX formats
- Historical backup retention
- Database accessible without application installation

## Tech Stack

- Java
- JavaFX
- Maven
- SQLite
- IntelliJ IDEA

## Special Features

**Diagnosis Patient Workflow**: Handles fixed-price diagnostic packages where patients pay a single amount covering all sessions, tests, reports, and feedback. Sessions are recorded as $0 since they're included in the package price.

## Status

In production use. Currently runs via Maven due to deployment complexities. A Python rewrite (ContabilidadClinica) is in development with additional features.
   
   ## Screenshots
![Sessions](https://github.com/user-attachments/assets/ee1d9c4b-41dd-4a34-9cf7-ed9d9ad3fb26)
![Payments](https://github.com/user-attachments/assets/174fd613-70ad-41eb-ba24-38c95d088692)
![MedicalReports](https://github.com/user-attachments/assets/0782682e-2686-4959-835f-f51e09f8d010)
![Accounting](https://github.com/user-attachments/assets/5e8cc024-b16c-4b40-8ef3-97681480afe4)
![PatientRegistry](https://github.com/user-attachments/assets/14e4a3e0-88ff-4426-954c-7674723e7d53)
  
   # About deployment

This application is currently deployed as a development build using Maven.

**Current setup:** Runs directly from source using a batch script
**In use:** Active production use in a medical clinic since October 2025

### Running from source
```bash
mvn clean javafx:run
```

Requirements: Java 21, Maven

   
