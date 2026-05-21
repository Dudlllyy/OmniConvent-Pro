import os
import json
import csv
import threading
import tkinter as tk
from tkinter import filedialog, messagebox
from PIL import Image
from moviepy import VideoFileClip, AudioFileClip
import customtkinter as ctk
from tkinterdnd2 import TkinterDnD, DND_FILES
import yaml
import markdown
import pandas as pd
from plyer import notification


class UniversalConverter:
    def __init__(self):
        self.image_formats = ['.png', '.jpg', '.jpeg', '.webp', '.bmp', '.ico']
        self.video_formats = ['.mp4', '.avi', '.mkv', '.mov']
        self.audio_formats = ['.mp3', '.wav', '.ogg', '.flac']
        self.gif_format = ['.gif']
        self.data_formats = ['.json', '.csv', '.yaml', '.yml']
        self.table_formats = ['.csv', '.xlsx']
        self.doc_formats = ['.md', '.html']

    def get_available_formats(self, input_ext):
        ext = input_ext.lower()
        if ext in self.image_formats:
            return [f for f in self.image_formats if f != ext]
        elif ext in self.video_formats:
            return self.audio_formats + ['.gif']
        elif ext in self.audio_formats:
            return [f for f in self.audio_formats if f != ext]
        elif ext in self.gif_format:
            return ['.mp4', '.png', '.jpg']
        elif ext == '.md':
            return ['.html']
        elif ext == '.xlsx':
            return ['.csv', '.json']
        elif ext == '.csv':
            return ['.xlsx', '.json']
        elif ext in ['.yaml', '.yml']:
            return ['.json']
        elif ext == '.json':
            return ['.csv', '.yaml', '.xlsx']
        return []

    def convert(self, input_path, output_path):
        _, input_ext = os.path.splitext(input_path.lower())
        _, output_ext = os.path.splitext(output_path.lower())

        if input_ext in self.image_formats and output_ext in self.image_formats:
            self._convert_image(input_path, output_path)
        elif input_ext in self.video_formats and output_ext in self.audio_formats:
            self._convert_video_to_audio(input_path, output_path)
        elif input_ext in self.video_formats and output_ext == '.gif':
            self._convert_video_to_gif(input_path, output_path)
        elif input_ext in self.audio_formats and output_ext in self.audio_formats:
            self._convert_audio(input_path, output_path)
        elif input_ext == '.gif' and output_ext == '.mp4':
            self._convert_gif_to_video(input_path, output_path)
        elif input_ext == '.gif' and output_ext in ['.png', '.jpg', '.jpeg']:
            self._convert_gif_to_image(input_path, output_path)
        elif input_ext == '.md' and output_ext == '.html':
            self._convert_md_to_html(input_path, output_path)
        elif (input_ext in self.table_formats or input_ext == '.json') and (
                output_ext in self.table_formats or output_ext == '.json'):
            self._convert_table_pandas(input_path, output_path, input_ext, output_ext)
        elif input_ext in ['.yaml', '.yml', '.json'] and output_ext in ['.yaml', '.yml', '.json']:
            self._convert_config(input_path, output_path, input_ext, output_ext)

        else:
            raise ValueError(f"Convert from {input_ext} в {output_ext} not supported.")

    def _convert_image(self, input_path, output_path):
        img = Image.open(input_path)
        if img.mode in ("RGBA", "P") and output_path.lower().endswith(('.jpg', '.jpeg')):
            img = img.convert("RGB")
        if output_path.lower().endswith('.ico'):
            img.save(output_path, format='ICO', sizes=[(32, 32), (64, 64), (128, 128)])
        else:
            img.save(output_path)

    def _convert_video_to_audio(self, input_path, output_path):
        video = VideoFileClip(input_path)
        if video.audio is None:
            video.close()
            raise ValueError("There is no audio line in the video.")
        video.audio.write_audiofile(output_path, logger=None)
        video.close()

    def _convert_video_to_gif(self, input_path, output_path):
        video = VideoFileClip(input_path)
        optimized_video = video.resized(width=480).with_fps(10)
        optimized_video.write_gif(output_path, logger=None)
        optimized_video.close()
        video.close()

    def _convert_audio(self, input_path, output_path):
        audio = AudioFileClip(input_path)
        audio.write_audiofile(output_path, logger=None)
        audio.close()

    def _convert_gif_to_video(self, input_path, output_path):
        video = VideoFileClip(input_path)
        video.write_videofile(output_path, codec="libx264", logger=None)
        video.close()

    def _convert_gif_to_image(self, input_path, output_path):
        img = Image.open(input_path)
        img.seek(0)
        if img.mode in ("RGBA", "P") and output_path.lower().endswith(('.jpg', '.jpeg')):
            img = img.convert("RGB")
        img.save(output_path)

    def _convert_md_to_html(self, input_path, output_path):
        with open(input_path, 'r', encoding='utf-8') as f:
            text = f.read()
        html = markdown.markdown(text)
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(html)

    def _convert_table_pandas(self, input_path, output_path, in_ext, out_ext):
        if in_ext == '.csv':
            df = pd.read_csv(input_path)
        elif in_ext == '.xlsx':
            df = pd.read_excel(input_path)
        elif in_ext == '.json':
            df = pd.read_json(input_path)

        if out_ext == '.csv':
            df.to_csv(output_path, index=False, encoding='utf-8')
        elif out_ext == '.xlsx':
            df.to_excel(output_path, index=False)
        elif out_ext == '.json':
            df.to_json(output_path, orient='records', force_ascii=False, indent=4)

    def _convert_config(self, input_path, output_path, in_ext, out_ext):
        with open(input_path, 'r', encoding='utf-8') as f:
            if in_ext == '.json':
                data = json.load(f)
            else:
                data = yaml.safe_load(f)
        with open(output_path, 'w', encoding='utf-8') as f:
            if out_ext == '.json':
                json.dump(data, f, ensure_ascii=False, indent=4)
            else:
                yaml.dump(data, f, allow_unicode=True, default_flow_style=False)


ctk.set_appearance_mode("Dark")
ctk.set_default_color_theme("blue")


class ModernConverterApp(ctk.CTk, TkinterDnD.DnDWrapper):
    def __init__(self):
        super().__init__()
        self.TkdndVersion = TkinterDnD._require(self)

        self.title("OmniConvert Pro")
        self.geometry("480x480")
        self.resizable(True, True)
        self.minsize(400, 400)
        if os.path.exists("icon.ico"):
            self.iconbitmap("icon.ico")

        self.converter = UniversalConverter()
        self.selected_files = []
        self.is_expanded = False

        self.main_frame = ctk.CTkFrame(self, corner_radius=15)
        self.main_frame.pack(pady=20, padx=20, fill="both", expand=True)

        custom_font = ctk.CTkFont(family="Segoe UI Black", size=26, weight="bold", slant="italic")
        btn_font = ctk.CTkFont(family="Segoe UI Black", size=16, weight="bold")

        self.title_label = ctk.CTkLabel(
            self.main_frame,
            text="OmniConvert Pro",
            font=custom_font,
            text_color="#3b82f6"
        )
        self.title_label.pack(pady=(15, 10))

        self.btn_select = ctk.CTkButton(
            self.main_frame,
            text="Select files",
            font=btn_font,
            command=self.choose_files,
            corner_radius=8,
            height=40,
            fg_color="#3b82f6",
            hover_color="#2563eb"
        )
        self.btn_select.pack(pady=(0, 15))
        self.accordion_frame = ctk.CTkFrame(self.main_frame, fg_color="transparent")
        self.accordion_frame.pack(fill="x", padx=15, pady=(0, 15))
        self.accordion_header = ctk.CTkFrame(self.accordion_frame, fg_color="transparent")
        self.accordion_header.pack(fill="x")

        self.lbl_file_count = ctk.CTkLabel(
            self.accordion_header,
            text="Drag files here",
            text_color="gray"
        )
        self.lbl_file_count.pack(side="left", padx=5)

        self.btn_toggle = ctk.CTkButton(
            self.accordion_header,
            text="▼",
            width=100, height=24,
            fg_color="transparent",
            hover_color="#334155",
            text_color="#94a3b8",
            font=ctk.CTkFont(size=12, weight="bold"),
            command=self.toggle_file_list
        )

        self.file_list_frame = ctk.CTkScrollableFrame(
            self.accordion_frame,
            height=100,
            corner_radius=8,
            fg_color="#1e293b"
        )

        self.lbl_format = ctk.CTkLabel(self.main_frame, text="Select format:", font=ctk.CTkFont(weight="bold"))
        self.lbl_format.pack()

        self.combo_format = ctk.CTkOptionMenu(
            self.main_frame,
            values=["-"],
            state="disabled",
            corner_radius=8,
            height=35,
            font=btn_font
        )
        self.combo_format.pack(pady=(5, 15))

        self.btn_convert = ctk.CTkButton(
            self.main_frame,
            text="Convert",
            font=btn_font,
            command=self.start_conversion,
            corner_radius=8,
            height=45,
            fg_color="#3b82f6",
            hover_color="#059669",
            state="disabled"
        )
        self.btn_convert.pack(pady=(0, 10))

        self.lbl_status = ctk.CTkLabel(self.main_frame, text="", text_color="white")
        self.lbl_status.pack()

        self.drop_target_register(DND_FILES)
        self.dnd_bind('<<Drop>>', self.drop_event)

    def toggle_file_list(self):
        if not self.selected_files:
            return

        self.is_expanded = not self.is_expanded
        if self.is_expanded:
            self.btn_toggle.configure(text="▲")
            self.file_list_frame.pack(fill="x", pady=(5, 0))
        else:
            self.btn_toggle.configure(text="▼")
            self.file_list_frame.pack_forget()

    def force_close_accordion(self):
        self.is_expanded = False
        self.btn_toggle.configure(text="▼")
        self.file_list_frame.pack_forget()
        self.btn_toggle.pack_forget()

    def drop_event(self, event):
        filepaths = self.tk.splitlist(event.data)
        self.add_files_to_queue(filepaths)

    def choose_files(self):
        filepaths = filedialog.askopenfilenames()
        if filepaths:
            self.add_files_to_queue(filepaths)

    def add_files_to_queue(self, filepaths):
        for f in filepaths:
            if f not in self.selected_files:
                self.selected_files.append(f)
        self.refresh_ui()

    def remove_file(self, filepath):
        if filepath in self.selected_files:
            self.selected_files.remove(filepath)
        self.refresh_ui()

    def refresh_ui(self):
        for widget in self.file_list_frame.winfo_children():
            widget.destroy()

        count = len(self.selected_files)

        if count == 0:
            self.lbl_file_count.configure(text="Drag files here", text_color="gray")
            self.force_close_accordion()
            self.combo_format.configure(values=["-"], state="disabled")
            self.combo_format.set("-")
            self.btn_convert.configure(state="disabled")
            self.lbl_status.configure(text="")
            return

        if count == 1:
            filename = os.path.basename(self.selected_files[0])
            display_name = filename if len(filename) < 25 else filename[:22] + "..."
            self.lbl_file_count.configure(text=f"Selected: {display_name}", text_color="white")
        else:
            self.lbl_file_count.configure(text=f"Selected files: {count} ", text_color="white")

        self.btn_toggle.pack(side="right")

        height = min(count * 35, 110)
        self.file_list_frame.configure(height=height)

        for file_path in self.selected_files:
            row_frame = ctk.CTkFrame(self.file_list_frame, fg_color="transparent")
            row_frame.pack(fill="x", pady=2)

            filename = os.path.basename(file_path)
            display_name = filename if len(filename) < 35 else filename[:32] + "..."

            lbl = ctk.CTkLabel(row_frame, text=display_name, anchor="w", text_color="#f8fafc")
            lbl.pack(side="left", fill="x", expand=True, padx=(5, 10))

            btn_remove = ctk.CTkButton(
                row_frame, text="✕", width=24, height=24, corner_radius=6,
                fg_color="#ef4444", hover_color="#b91c1c", font=ctk.CTkFont(weight="bold"),
                command=lambda p=file_path: self.remove_file(p)
            )
            btn_remove.pack(side="right")

        common_formats = None
        for path in self.selected_files:
            _, ext = os.path.splitext(path)
            formats_for_this_file = set(self.converter.get_available_formats(ext))

            if common_formats is None:
                common_formats = formats_for_this_file
            else:
                common_formats = common_formats.intersection(formats_for_this_file)

        available_formats = list(common_formats) if common_formats else []

        if available_formats:
            self.combo_format.configure(values=available_formats, state="normal")
            self.combo_format.set(available_formats[0])
            self.btn_convert.configure(state="normal")
            self.lbl_status.configure(text="")
        else:
            self.combo_format.configure(values=["-"], state="disabled")
            self.combo_format.set("-")
            self.btn_convert.configure(state="disabled")
            if count > 1:
                self.lbl_status.configure(text="Format conflict! Delete unnecessary files.",
                                          text_color="#f59e0b")
            else:
                self.lbl_status.configure(text="Format not supported", text_color="#ef4444")

    def start_conversion(self):
        if not self.selected_files or self.combo_format.get() == "-":
            return

        target_ext = self.combo_format.get()
        self.btn_convert.configure(state="disabled")

        for widget in self.file_list_frame.winfo_children():
            for child in widget.winfo_children():
                if isinstance(child, ctk.CTkButton):
                    child.configure(state="disabled")

        self.lbl_status.configure(text=f"Running...", text_color="#3b82f6")
        thread = threading.Thread(target=self._run_conversion_thread, args=(self.selected_files, target_ext))
        thread.start()

    def _run_conversion_thread(self, input_paths, target_ext):
        total = len(input_paths)
        success_count = 0
        errors = []
        labels = {
            '.png': 'picture', '.jpg': 'picture', '.jpeg': 'picture', '.webp': 'picture', '.bmp': 'picture',
            '.ico': 'icon', '.mp4': 'video', '.avi': 'video', '.mkv': 'video', '.mov': 'video',
            '.mp3': 'audio', '.wav': 'audio', '.ogg': 'audio', '.flac': 'audio', '.gif': 'GIF',
            '.json': 'config', '.yaml': 'config', '.yml': 'config', '.csv': 'table', '.xlsx': 'table',
            '.html': 'document'
        }

        category_label = labels.get(target_ext.lower(), 'converted')

        for i, input_path in enumerate(input_paths):
            self.after(0, self.lbl_status.configure, {"text": f"Conversion... {i + 1}/{total}"})
            base_path, _ = os.path.splitext(input_path)
            output_path = f"{base_path} ({category_label}){target_ext}"

            try:
                self.converter.convert(input_path, output_path)
                success_count += 1
            except Exception as e:
                errors.append(f"{os.path.basename(input_path)}: {e}")

        self.after(0, self._on_finish, success_count, total, errors)

    def _on_finish(self, success_count, total, errors):
        self.btn_convert.configure(state="normal")

        if success_count == total:
            self.lbl_status.configure(text="All files saved successfully!", text_color="#10b981")
            self.selected_files.clear()
            self.refresh_ui()
            notify_title = "Success!"
            notify_message = f"Conversion completed: {total} "
        elif success_count > 0:
            self.lbl_status.configure(text=f"Ready: {success_count} of {total} (there are errors)", text_color="#f59e0b")
            messagebox.showwarning("Partial success", "Some files were not converted:\n" + "\n".join(errors[:5]))
            notify_title = "Completed with errors"
            notify_message = f"Ready: {success_count} of {total}."
        else:
            self.lbl_status.configure(text="Conversion error", text_color="#ef4444")
            messagebox.showerror("Error", "No files have been converted.\n" + "\n".join(errors[:5]))
            self.refresh_ui()
            notify_title = "Error"
            notify_message = "No files have been converted."

        try:
            notification.notify(
                title=f"OmniConvert Pro: {notify_title}",
                message=notify_message,
                app_name="OmniConvert",
                timeout=5
            )
        except Exception as e:
            pass


if __name__ == "__main__":
    app = ModernConverterApp()
    app.mainloop()
