import re

def fix_xml_closing_tags(file_path):
    """Fix XML tags that are missing closing '/>' after removing fontFamily attribute"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Pattern to find lines ending with attribute value followed by newline and whitespace + <
        # This indicates a missing closing tag
        pattern = r'(android:\w+="[^"]+")(\s*\n\s*)(<[A-Z])'

        def replacer(match):
            attr = match.group(1)
            whitespace = match.group(2)
            next_tag = match.group(3)
            return f'{attr} />{whitespace}{next_tag}'

        new_content = re.sub(pattern, replacer, content)

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)

        print(f"Fixed closing tags in: {file_path}")
        return True
    except Exception as e:
        print(f"Error: {str(e)}")
        return False

if __name__ == "__main__":
    files = [
        r"D:\app\HealthTips-App-\app\src\main\res\layout\activity_about.xml",
        r"D:\app\HealthTips-App-\app\src\main\res\layout\activity_create_support_ticket.xml"
    ]

    for file_path in files:
        fix_xml_closing_tags(file_path)

