## 📱 MedScope – Mobile ECG Monitoring App

**MedScope** is a mobile application designed to provide a convenient and efficient way to monitor, visualize, and analyze ECG (Electrocardiogram) signals in real time. It supports signal acquisition via Bluetooth, AI-based ECG classification, and user management tailored for different roles (Doctor, Patient, Receptionist, Lab, Admin).

<img width="912" height="912" alt="Logo" src="https://github.com/user-attachments/assets/643a6ede-5e15-4dc2-b1c8-8c2ead549e58" />
<h2>MedScope Logo</h2>


---

### 🚀 Features

* 🔌 **Bluetooth Connection**: Connects to ECG sensors for real-time signal streaming
* 📊 **Signal Visualization**: Graphical display of ECG signals (GraphView)
* 🧠 **AI Integration**: Uses deep learning (TFLite) to classify ECG patterns (Normal, AFib, etc.)
* 👨‍⚕️ **Multi-User System**: Supports roles like Doctor, Patient, Receptionist, Lab Technician, and Admin
* 📅 **Appointment Booking**: Patients can request and manage appointments
* 🔔 **Notifications**: Local reminders and alerts
* 🖼️ **Clinic Ads Display**: ViewPager for in-app ad campaigns per clinic
* 🌐 **Laravel API**: Fully integrated backend for login, records, and scheduling

---

### 🛠️ Tech Stack

* **Frontend**: Android (Java/Kotlin)
* **AI**: TensorFlow Lite (TFLite), CNN model trained on MIT-BIH ECG dataset
* **Backend**: Laravel + MySQL
* **Communication**: REST API + Bluetooth SPP
* **UI Components**: GraphView, ViewPager, Material Design

---

### 📸 Screenshots

> ![User fingerprint authentication](https://github.com/user-attachments/assets/d463e0ee-e77e-4bb2-a65b-b3e244854a13)
> ![Patient home screen](https://github.com/user-attachments/assets/9f7753ff-8618-42c0-839b-c14de8cfc25e)
> ![User profile](https://github.com/user-attachments/assets/e971278d-69d7-42c8-af28-b013ba1a0b44)
> ![Patient monitoring screen](https://github.com/user-attachments/assets/72f6d943-8596-43a6-b8dd-a763ae700459)
>![Admin screen](https://github.com/user-attachments/assets/5c122194-24a7-4056-8da8-b4153adc7ac0)
> ![User QR Code](https://github.com/user-attachments/assets/89ef02e5-670a-4c5b-af64-0b426045db23)
> ![Doctor home screen](https://github.com/user-attachments/assets/4818ea7f-a914-48ea-9f07-e89f7ddac9d7)

---

### ⚙️ How to Build

1. Clone this repo:

   ```bash
   git clone https://github.com/yourusername/MedScope.git
   ```
2. Open in **Android Studio**
3. Sync Gradle and run on emulator or physical device (with Bluetooth enabled)
4. Backend: configure API base URL in Retrofit settings

---

### 🤖 AI Model

* Input Shape: `(1, 186, 1)`
* Output Classes: 5 ECG categories
* Trained using MIT-BIH arrhythmia dataset
* Deployed as `.tflite` model inside Android app

---

### 📂 Folder Structure

```
MedScope/
│
├── app/
│   ├── java/
│   │   └── com.example.medscope/
│   │       ├── ui/
│   │       ├── model/
│   │       ├── bluetooth/
│   │       └── ml/
│   ├── res/
│   └── AndroidManifest.xml
├── build.gradle
└── README.md
```

---

### 📬 Contact

For more information, contact \[[dalil.moulayabdelhakim@gmail.com](mailto:dalil.moulayabdelhakim@gmail.com)]
Or visit: \[your-portfolio.com]

---

### 📄 License

MIT License
