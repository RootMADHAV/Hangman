import re
f=open('app/src/main/kotlin/com/LetterQuest/data/repository/WordCatalog.kt',encoding='utf-8').read()

# Check all categories and their counts
all_cats = [
    'animals', 'movies', 'tv_series', 'books', 'countries', 'cities', 'sports',
    'foods', 'music', 'landmarks', 'nature', 'science', 'technology',
    'professions', 'space', 'mythology', 'geography', 'famous_quotes'
]

for cat in all_cats:
    pattern = r'Word\("([^"]+)",\s+Difficulty\.\w+,\s+"[^"]*",\s+"' + re.escape(cat) + r'"\)'
    matches = re.findall(pattern, f)
    status = "OK" if len(matches) >= 50 else f"NEEDS {50-len(matches)} more"
    print(f'{cat}: {len(matches)} words - {status}')

# Count total
pattern = r'Word\("'
total = len(re.findall(pattern, f))
print(f'\nTotal words: {total}')
