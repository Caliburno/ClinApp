# Clinic Financial Manager
   
   Desktop application for managing a psychologist clinic finances built with JavaFX.
   
   ## Features
   - Patient registry: Add patient profiles to keep track of each individualy. Specify type of patient, type of session and amount charged.
   - Sessions tracking: Add sessions for the patients, clasified by type of session. Can be set as "paid" so it's not contabilized towards patient debt.
   - Medical reports: Add and keep track of medical reports for patients. Set status of payment(partial, total, etc.) set status of report(tests needed, finished, delivered, etc.)
   - Payments: Add payments for the patients profiles. The payments are authomaticaly deducted from patients debt.
   - Accounting: Get a view only report of the total amount colected, and broken down by type of patient, and the amount owed. There is also a table showing debt by patient and by type.
   - Backup saved periodically in .db and .xlsx files simultaneously, leaving a historical registry for auditing. Program creates a new save file on exit.
   
   ## Tech Stack
   Java 21, JavaFX, SQLite, Maven
   
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

Future improvements: Package as standalone executable with jlink/jpackage.

### Running from source
```bash
mvn clean javafx:run
```

Requirements: Java 21, Maven

   
