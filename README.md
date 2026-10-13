# Capstone-Project-Fall-2026-

# CampusPath – Student Academic Planning Application

## Project Overview

CampusPath is a web application designed to help college students manage their academic schedules, degree progress, work commitments, and personal responsibilities in one place. The goal is to make college planning easier by helping students organize their schedules, track their academic progress, and make better decisions about their courses.

As the project develops, CampusPath will include features such as automatic schedule generation, course recommendations, degree tracking, and personalized planning tools.

## Sprint 1 Overview

Sprint 1 focuses on building the foundation of CampusPath by developing the main pages and setting up user authentication. Our goal is to allow students to create an account, log in securely, and access an initial homepage.

### Sprint 1 Features

**1. User Registration**
- Create a registration page for new students.
- Collect student information, including full name, email, EUID, and password.
- Set up account registration and email verification.

**2. User Login**
- Develop a login page for existing users.
- Allow students to sign in using their email and password.
- Connect the authentication process to AWS Cognito.

**3. Initial Homepage**
- Create the basic CampusPath homepage/dashboard.
- Provide a starting point for future academic planning features.

**4. AWS Authentication**
- Configure AWS Cognito for student registration and login.
- Create a User Pool and App Client.
- Test account creation and email verification.
- Prepare authentication for integration with the frontend and backend.

## Technologies Used

- **Frontend:** HTML, CSS, JavaScript
- **Backend:** Java, Spring Boot
- **Database:** PostgreSQL
- **Cloud Services:** AWS Cognito, EC2, RDS, S3, Lambda, CloudWatch
- **Version Control:** Git and GitHub

## Current Progress

During Sprint 1, the team is working on developing the registration page, login page, and initial homepage.

The AWS Cognito setup has been completed, and student registration and email verification have been successfully tested using the AWS-hosted login page.

Integration between the frontend, backend, and AWS authentication system is still in progress.

## Future Development

After Sprint 1, the team plans to expand CampusPath by adding academic planning features, including degree progress tracking, course recommendations, automatic schedule generation, and tools for managing student commitments.

Our overall goal is to create a user-friendly application that helps students balance their academic and personal responsibilities while staying on track toward graduation.

## How to Run

Step 1: Download the Repository
1. Open the CampusPath repository
2. Select the branch you want to test using the branch dropdown.
3. Click the green Code button.
4. Select Download ZIP.
5. Locate the downloaded ZIP file on your computer.
6. Extract the ZIP file to access the project folder.

Step 2: Locate the Website Files
1. Open the downloaded project folder and locate login.html.
2. Make sure the HTML, CSS, and JavaScript files remain in their correct locations so the website can load its styling and functionality.

Step 3: Open the Website
Option 1: Open Directly in a Browser

For Mac users:
1. Open Finder.
2. Navigate to the extracted project folder.
3. Locate login.html.
4. Right-click the file and select Open With.
5. Choose Google Chrome, Safari, or another browser.


For Windows users:
1. Open File Explorer.
2. Navigate to the extracted project folder.
3. Locate login.html.
4. Double-click the file or select a browser using Open with.
The CampusPath login page should now appear.


Option 2: Use Visual Studio Code with a Live Server
1. Open Visual Studio Code.
2. Select File > Open Folder and open the project folder.
3. Install the Live Server extension if it is not already installed.
4. Right-click login.html.
5. Select Open with Live Server.

## How to Test the Website
Login Page
1. Open login.html.
2. Enter a sample email address and password.
3. Click the Log in button.
4. The website should redirect to dashboard.html.
