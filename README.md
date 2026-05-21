# 🔄 OmniConvert Pro

**OmniConvert Pro** is a universal, fast, and modern desktop file converter. It supports batch processing, drag-and-drop functionality, and a smart format recognition system.

Built with a strong focus on UX/UI, it features an adaptive file "accordion" queue, a sleek dark theme, and native Windows notifications. The project is fully implemented in two parallel versions: **Python** and **Java**.

---

## ✨ Key Features
- 🖼 **Images:** `PNG`, `JPG/JPEG` (with auto-fill for transparency), `BMP`, `WEBP`, `ICO`.
- 🎬 **Media (via FFmpeg):** Convert `MP4`, `AVI`, `MKV` to `MP3` or optimized `GIF`.
- 📄 **Data & Configs:** Mutual conversion of `JSON`, `YAML`, `CSV`, `XLSX`.
- 📦 **Batch Processing:** Drop dozens of files of various formats at once — the engine will automatically find common extensions and block incompatible options.
- 🎨 **Modern UI:** Adaptive window sizing, smooth accordion list for queued files, and targeted removal of individual files.

---

## 🚀 Installation & Usage

### Global Dependencies
To process media files, the application requires **FFmpeg**.
1. Download the `ffmpeg.exe` binary (e.g., from [Gyan.dev](https://www.gyan.dev/ffmpeg/builds/)).
2. Place the `ffmpeg.exe` file directly into the root folder of the project.

### 📦 Ready-to-Use Executable (Windows)
If you don't want to install Python or Java dependencies, you can download the pre-compiled standalone version.

1. Go to the **[Releases](../../releases)** section on the right side of this repository.
2. Download the latest `OmniConvert_Pro.exe`.
3. Download `ffmpeg.exe` and place it in the exact same folder as your `OmniConvert_Pro.exe`.
4. Run the program and enjoy!
### Python Version 🐍
Ensure you have Python 3.8+ installed.
```bash
# Install required dependencies
pip install customtkinter tkinterdnd2 pillow moviepy pyyaml markdown pandas openpyxl plyer

# Run the application
python convent.py
