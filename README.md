# 🎮 Hangman — Mobile Word Guessing Game

A modern, feature-rich Android implementation of the classic Hangman word-guessing game, built with **Kotlin**, **Jetpack Compose**, and **Material Design 3**.

## 🎯 Core Gameplay

Guess a hidden word by selecting letters before running out of attempts.

- **5 difficulty levels** with progressive word complexity
- **17+ word categories** (Animals, Movies, Books, Mythology, Geography, etc.)
- **Dynamic hints system** using in-game token currency
- **Win streaks** — auto-continue to next word without leaving gameplay
- **Daily challenges** with persistent streak tracking

## ✨ Key Features

### Game Mechanics
✅ **No-repeat word selection** — previous word excluded from next draw  
✅ **Hangman drawing** — visual builds with each wrong guess  
✅ **Dynamic scoring** — based on difficulty, attempts, time  
✅ **Statistics tracking** — wins, losses, accuracy, category progress  
✅ **Word difficulty indicator** — shows difficulty level during gameplay  
✅ **Famous Quotes category** — 18 multi-word phrases from movies & landmarks  

### Challenge Modes
🎮 **Classic Mode** — traditional hangman  
⏱️ **Timed Mode** — solve within 60 seconds  
❌ **Limited Guesses** — only 3 wrong attempts  
📂 **Category Challenge** — complete all words in chosen category  

### Economy System
🪙 **Tokens** earned from play (correct guesses +2, wins +25, losses +5)  
💰 **Coin bundles** for cosmetic purchases ($0.99–$7.99 demo packs)  
🎁 **Daily login rewards** (50 base + 10/day streak, capped at 100)  

### Power-ups (Single-Use Per Game)
❤️ **Second Wind** (500 🪙) → +1 attempt  
🏷️ **Bargain Hunter** (800 🪙) → hints 25% cheaper  
📈 **Double Down** (1200 🪙) → +50% score multiplier  

### Hints (Consumed Per-Game)
💡 **Show Hint** (8 🪙) → reveal word clue  
🔤 **Reveal Letter** (15 🪙) → uncover one letter  
➕ **Extra Attempt** (20 🪙) → +1 wrong guess  
⏭️ **Skip Word** (30 🪙) → new word, keep score  

### Achievements & Progression
🏆 **10 achievement milestones** — unlock badges and earn reward tokens  
📊 **Category-specific statistics** — track wins/losses/accuracy per category  
🏅 **Enhanced leaderboards** — sort by score, time, date, or category  
📈 **Difficulty levels in stats** — breakdown of Easy/Normal/Hard performance  

### Player Customization
👤 **Custom nicknames** — personalize your player profile  
🎨 **10 unique avatars** — choose from Player, Champion, Genius, Speed Demon, Lucky, Dragon, Phoenix, Knight, Wizard, Star  
📋 **Player profiles** — persistent account with earned tokens and playtime  

### Sound System
🎵 **8 independent sound controls** — toggle individual sound types  
✓ Correct/Incorrect guess sounds  
🎉 Win/Lose celebration sounds  
🔘 UI button click feedback  
🏆 Milestone & level-up notifications  
🎵 **Background music** — optional soothing background playback  

### UI/UX
🎨 Material Design 3 with light/dark mode  
✨ Smooth animations & sound feedback  
🎪 Win-celebration overlay with auto-load  
📱 Responsive, ad-integrated layout  
⚙️ Advanced sound & theme customization  
🌙 **Dark theme optimization** — enhanced text contrast in Profile & Leaderboard  
📂 **Category selection screen** — choose word source before play  

## 🏗️ Architecture

**MVVM + Clean Repository Pattern**

```
domain/    → Business logic & interfaces
data/      → Persistence & implementations
ui/        → Screens, ViewModels, Components
di/        → Hilt dependency injection
```

**Stack:** Kotlin 1.9+ · Jetpack Compose · Material 3 · Room + DataStore · Coroutines/Flow · Hilt

## 📱 Screens

| | |
|--|--|
| Home | Token balance, daily reward badge, play buttons, nav grid |
| Gameplay | Hangman drawing, word display, hints, alphabet grid |
| Win Celebration | Overlay: streak count, tokens earned, next-word button |
| Loss Results | Word reveal, stats, retry/new-game options |
| Shop | Daily rewards, coin bundles, power-up purchases |
| Statistics | Win/loss, accuracy, category breakdown, history |
| Achievements | Badges & milestone tracking |
| Settings | Sound, theme, preferences |

## 🎯 Word Catalog

**~300+ words** across **17 categories:**

🐾 Animals (32) · 🎬 Movies (22) · 📺 TV (11) · 📚 Books (12) · 🌍 Countries (16) · 🏙️ Cities (15) · ⚽ Sports (15) · 🍕 Foods (21) · 🎵 Music (14) · 🗼 Landmarks (10) · 🌿 Nature (15) · 🔬 Science (13) · 💻 Tech (13) · 👷 Professions (14) · 🚀 Space (19) · ⚡ Mythology (15) · 🗺️ Geography (11)

## 🛠️ Build & Run

**Requirements:** Android SDK 35 · Min SDK 29 · Kotlin 1.9+ · JDK 17

```bash
./gradlew build                 # Build
./gradlew installDebug          # Install to device
./gradlew test                  # Unit tests
./gradlew connectedAndroidTest  # Integration tests
```

## 🔄 Gameplay Loop

Home → Setup (difficulty/category) → Play → Win (overlay continues) or Lose (results screen) → Repeat

## 📊 Game Balance

| | |
|--|--|
| Starting balance | 0 🪙 |
| Correct guess | +2 🪙 |
| Win | +2 🪙 |
| Loss | +0 🪙 |
| Daily base | 20 🪙 |
| Streak bonus | +10/day (max 100) |
| Power-up costs | 500–1200 |
| Hint costs | 8–30 |

## 📋 Milestone 5 Updates

✅ Win streaks (auto-continue overlay)  
✅ No-repeat word selection  
✅ Show Hint power-up  
✅ Single-use power-ups per game  
✅ Increased power-up costs (4–10×)  
✅ Daily login rewards with streaks  
✅ Coin purchase wall (IAP demo)  
✅ Hangman drawing (progressive)  
✅ Improved UI (hero card, better shop, stat chips)  
✅ 300+ words in 17 categories  
✅ Settings button fix (no ad overlap)  

## 📋 Milestone 6 Updates (August 2026)

✅ Famous Quotes & Landmarks category (18 multi-word phrases)  
✅ Category selection screen before gameplay  
✅ Challenge modes (Classic, Timed, Limited Guesses, Category Challenge)  
✅ Achievements system (10 unique milestones with token rewards)  
✅ Enhanced leaderboards (sort by score/time/date/category, time filters)  
✅ Word difficulty indicator on gameplay screen  
✅ Category-specific statistics (win rate, accuracy, difficulty breakdown)  
✅ Sound system overhaul (8 independent sound types with toggles)  
✅ Background music framework (soothing playback when enabled)  
✅ Player customization (10 avatars, custom nicknames, player profiles)  
✅ Dark theme optimizations (Profile & Leaderboard sections)  
✅ APK build successful (34 MB release build)  

## 🚀 Future

Online leaderboards · Multiplayer · Voice input · i18n · AI difficulty scaling · UI screens for achievements/challenges

---

Built with ❤️ using Kotlin & Jetpack Compose
