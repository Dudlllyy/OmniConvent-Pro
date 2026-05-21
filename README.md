# 🔄 OmniConvert Pro

**OmniConvert Pro** is a universal, fast, and modern desktop file converter. It supports batch processing, drag-and-drop functionality, and a smart format recognition system.

Built with a strong focus on UX/UI, it features an adaptive file "accordion" queue, a sleek dark theme, and native OS notifications. The project is fully implemented in two parallel versions: **Python** and **Java**.

---

## ✨ Key Features
- 🖼 **Images:** `PNG`, `JPG/JPEG` (with auto-fill for transparency), `BMP`, `WEBP`, `ICO`.
- 🎬 **Media:** Convert `MP4`, `AVI`, `MKV` to `MP3` or optimized `GIF`.
- 📄 **Data & Configs:** Mutual conversion of `JSON`, `YAML`, `CSV`, `XLSX`.
- 📦 **Batch Processing:** Drop dozens of files of various formats at once — the engine will automatically find common extensions and block incompatible options.
- 🎨 **Modern UI:** Adaptive window sizing, smooth accordion list for queued files, and targeted removal of individual files.

---

## 📥 Ready-to-Use Packages (No Setup Required)

If you just want to use the app without installing development environments, you can download the pre-compiled versions directly from the **[Releases](../../releases)** section.

### For Windows Users (Python Standalone)
1. Go to the Releases page and download the latest `OmniConvert_Pro.exe`.
2. Run the program and enjoy! 
*(Note: This version is completely standalone. It contains built-in FFmpeg and requires no extra installations).*

### For Cross-Platform Users (Java Version)
1. Ensure you have **Java 17 or higher** installed on your system.
2. Go to the Releases page and download `OmniConvert_Pro.jar`.
3. Download the `ffmpeg` binary (e.g., from [Gyan.dev](https://www.gyan.dev/ffmpeg/builds/) for Windows or via package manager for Linux/macOS) and place it in the exact same folder as the `.jar` file.
4. Run it by double-clicking the file or via terminal:
   ```bash
   java -jar OmniConvert_Pro.jar

### 🚀 Building from Source

If you want to run or build the project from the source code, follow these instructions:
Python Version 🐍

Ensure you have Python 3.8+ installed.
   ```bash
  # Clone the repository
  git clone [https://github.com/YOUR_USERNAME/OmniConvert.git](https://github.com/YOUR_USERNAME/OmniConvert.git)
  cd OmniConvert
  
  # Install required dependencies
  pip install customtkinter tkinterdnd2 pillow moviepy pyyaml markdown pandas openpyxl plyer
  
  # Run the application
  python main.py
  ```
Java Version ☕

Requires JDK 17+ and Maven. The project utilizes FlatLaf for a modern dark interface and TwelveMonkeys for extended image format support.
  ```bash

  # Clone the repository
  git clone [https://github.com/YOUR_USERNAME/OmniConvert.git](https://github.com/YOUR_USERNAME/OmniConvert.git)
  cd OmniConvert

  # Install dependencies and build via Maven
  mvn clean package

  # Run the application (ensure ffmpeg.exe is in the root folder)
  mvn exec:java -Dexec.mainClass="com.omniconvert.OmniConvertApp"
  ```
🛠 Tech Stack

    Python: CustomTkinter, TkinterDnD, MoviePy, Pandas, Pillow.

    Java: Swing + FlatLaf, Jackson, TwelveMonkeys ImageIO, Maven Assembly Plugin.

Developed with passion by Artem. Ready for production.

