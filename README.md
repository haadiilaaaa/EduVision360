\# EduVision360



EduVision360 is an AI-powered educational management and learning analytics platform designed to improve student engagement, academic monitoring, attendance tracking, and early risk detection.



The system integrates a React frontend, Spring Boot backend, and Python-based AI/ML services to provide a smart educational environment for students, teachers, and administrators.



\## Project Overview



EduVision360 includes:



\- secure authentication and role-based access control

\- OTP registration and password reset

\- student, teacher, and admin dashboards

\- face registration and attendance support

\- class session scheduling and automation

\- learning materials and view tracking

\- announcements and feedback

\- engagement monitoring

\- dropout prediction

\- AI tutor support

\- dashboard and email notifications



\## Repository Structure



```text

EduVision360/

├── README.md

├── .gitignore

├── eduvision360-frontend/

├── eduvision360/

│   └── eduvision360/

├── student\_dropout\_prediction/

└── face\_recognition\_ai/

Technologies Used
Frontend

React

JavaScript

CSS

Axios

Backend

Spring Boot

Java

Maven

MongoDB

JWT Authentication

AI / ML

Python

Scikit-learn / XGBoost / CatBoost

Face recognition libraries

Setup Instructions
Frontend
cd eduvision360-frontend
npm install
npm start
Backend
cd eduvision360/eduvision360
mvn spring-boot:run
Dropout Prediction Module
cd student_dropout_prediction
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python src/predict.py
Face Recognition Module
cd face_recognition_ai
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python app.py
Branching Strategy

Main branches:

main

develop

Feature branches:

feature/backend

feature/frontend

feature/ml-dropout

feature/face-ai

feature/docs

Versioning

Example tags:

v0.1.0

v0.2.0

v0.3.0

v0.4.0

v1.0.0

Author

Hadila Fassy

License

This project is for academic and educational purposes.

