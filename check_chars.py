import re
f=open('C:/Users/gamer/Downloads/freemodeldev/hangman/app/src/main/kotlin/com/LetterQuest/data/repository/WordCatalog.kt',encoding='utf-8').read()

# Check mythology and geography categories for non-English/Hindi words
for cat in ['mythology', 'geography', 'professions']:
    pattern = r'Word\("([^"]+)",\s+Difficulty\.(\w+),\s+"([^"]*)",\s+"' + re.escape(cat) + r'"\)'
    matches = re.findall(pattern, f)
    # Check for non-ASCII characters
    for w, d, h in matches:
        if any(ord(c) > 127 for c in w):
            print(f"NON-ASCII in {cat}: {w}")
    if any(any(ord(c) > 127 for c in w) for w, d, h in matches):
        print(f"  ^-- {cat} has non-ASCII words")
    else:
        print(f"{cat}: all ASCII OK ({len(matches)} words)")
