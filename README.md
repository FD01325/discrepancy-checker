# Timesheet Discrepancy Checker

## Overview
The **Timesheet Discrepancy Checker** is a web application that allows us to upload two timesheet files (Prism and Beeline) and generate a discrepancy report in Excel format. The application provides a user-friendly interface to upload files, track uploads, and download the generated report with clear notifications.

## Features
- **Upload Prism Timesheet**: Upload an Excel file for Prism timesheet data.
- **Upload Beeline Timesheet**: Upload an Excel file for Beeline timesheet data.
- **Generate Discrepancy Report**: Compare both timesheets and generate an Excel report.
- **Download Report**: Download the discrepancy report after processing.
- **User Notifications**: Display pop-ups to confirm successful report generation.

## Technologies Used
### Frontend (React.js)
- React.js (with functional components and hooks)
- Tailwind CSS for UI styling
- Axios for API requests
- React Toastify for notifications

### Backend (Spring Boot - Java)
- Spring Boot REST API
- Apache POI for Excel file handling
- Multipart file processing
- CORS configuration for frontend-backend communication


## Installation & Setup
### Prerequisites
- Node.js and npm
- Java 17 and Maven
- Spring Boot setup

### Backend Setup
1. Build the Spring Boot application:
   ```sh
   mvn clean install
   ```
2. Run the backend application:
   ```sh
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the `frontend/` directory:
   ```sh
   cd frontend
   ```
2. Install dependencies:
   ```sh
   npm install
   ```
3. Start the frontend application:
   ```sh
   npm start
   ```

## Usage
1. Open `http://localhost:3000` in your browser.
2. Upload the **Prism** and **Beeline** timesheet files.
3. Click the **Generate Report** button.
4. Download the discrepancy report once the API processing is complete.
5. View success notifications for uploads and report generation.

## API Endpoints
| Method | Endpoint | Description |
|--------|---------|-------------|
| POST | `/api/timesheet/compare` | Uploads Prism and Beeline files and returns a discrepancy report |
