import re
import sys

def remove_inter_font(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Remove fontFamily attribute using regex
        # This will preserve the line structure and only remove the fontFamily attribute
        pattern = r'\s*android:fontFamily="@font/inter_[^"]+"\s*'
        new_content = re.sub(pattern, '', content)

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)

        print(f"Successfully processed: {file_path}")
        return True
    except Exception as e:
        print(f"Error processing {file_path}: {str(e)}")
        return False

if __name__ == "__main__":
    files = [
        r"D:\app\HealthTips-App-\app\src\main\res\layout\activity_about.xml",
        r"D:\app\HealthTips-App-\app\src\main\res\layout\activity_create_support_ticket.xml"
    ]

    for file_path in files:
        remove_inter_font(file_path)

