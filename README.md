# 🤖 DocuMind AI - Backend
Intelligent document management system powered by Google Gemini AI.

## 🌍 Live Project
**Frontend:** [https://documind-frontend-rust.vercel.app](https://documind-frontend-rust.vercel.app)  
**Backend API:** [https://documind-backend-production-3e92.up.railway.app](https://documind-backend-production-3e92.up.railway.app)

## 🚀 Features
- **JWT Authentication** - Secure user registration and login.
- **AI-Powered Processing** - **Automatic Summarization:** 3-5 concise bullet points using Gemini 1.5 Flash.
  - **Intelligent Classification:** Auto-categorizes as Invoice, Resume, Report, etc.
  - **Document Q&A:** Context-aware chat interface to ask questions about specific files.
- **Multi-Format Support** - PDF (PDFBox), DOCX (Apache POI), and TXT extraction.
- **Relational Storage** - MySQL database for user data and document metadata.

## 🛠️ Tech Stack
- **Backend:** Java 21, Spring Boot 4.0.3, Spring Security 6 (JWT)
- **Database:** MySQL 8.0 (Hosted on Railway)
- **AI Engine:** Google Gemini AI API
- **Tools:** Apache PDFBox, Apache POI, Gson, Maven

## 📦 Key API Endpoints
### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and receive JWT token

### AI & Document Features
- `POST /api/documents/upload` - Upload & Auto-Process
- `POST /api/ai/ask/{id}` - Ask questions about a specific document
- `POST /api/ai/summarize/{id}` - Generate a new summary

<img width="1366" height="725" alt="Sign Up" src="https://github.com/user-attachments/assets/f7999294-d556-41ba-ad37-8e2a3437ed66" />
<img width="1366" height="723" alt="Login" src="https://github.com/user-attachments/assets/061f52f1-442e-4341-a103-7385ae5038e1" />
<img width="1366" height="723" alt="Dashboard" src="https://github.com/user-attachments/assets/85c442b7-6bb4-4514-b2ac-516aff6d5ffa" />
<img width="1366" height="723" alt="Chat" src="https://github.com/user-attachments/assets/40cdf5aa-7eef-44b2-bc9d-560ab48c9289" />

## 👨‍💻 Author
**Poovarasan K**
- **LinkedIn:** [linkedin.com/in/poovarasan-k-dev](https://linkedin.com/in/poovarasan-k-dev)
- **GitHub:** [Poovarasan47](https://github.com/Poovarasan47)

## 📄 License
This project is for portfolio purposes.

---
*Developed as a full-stack showcase of AI integration and secure cloud deployment.*
