# Project Name

## Description

This project is an Android application designed to manage student information and statistics within an educational institution. It utilizes Room as the local database for data persistence, allowing for efficient storage and retrieval of data related to students, majors, classes, and academic years.

## Features

- User authentication through a login screen.
- Student management with options to add, edit, and delete student records.
- Statistical analysis of students, majors, and lecturers displayed through charts.
- Data persistence using Room for efficient local storage.
- Intuitive user interface for easy navigation and data entry.

## Architecture

This project follows the MVVM (Model-View-ViewModel) architecture, ensuring a separation of concerns and facilitating easier testing and maintenance.

## Libraries Used

- **Room**: A persistence library that provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.
- **Coroutines**: For asynchronous programming, enabling background operations without blocking the UI thread.
- **MPAndroidChart**: For creating beautiful charts and visualizing statistics.

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git

2. Open the project in Android Studio.

3. Sync the Gradle files to ensure all dependencies are resolved.

4. Build and run the application on an emulator or a physical device.


## Database Schema

The following entities are defined in the Room database:

- **Student**: Represents student data, including relationships with majors and classes.
- **Major**: Represents different fields of study within the institution.
- **Class**: Represents classes offered in the institution.
- **AcademicYear**: Represents different academic years for managing student records.

