# DuelUp - Asset Requirements

All assets listed below are needed for the full app experience. Place them in the specified directories.

---

## Lottie Animations

Location: `app/src/main/res/raw/`

| File Name | Description | Where Used | Duration |
|-----------|-------------|------------|----------|
| `lottie_splash_logo.json` | App logo with pulsing glow effect | Splash Screen | 1.5s loop |
| `lottie_matchmaking_search.json` | Radar/pulse scanning animation | Matchmaking Screen (searching) | Loop |
| `lottie_matchmaking_found.json` | VS explosion/flash animation | Matchmaking Screen (match found) | 2s one-shot |
| `lottie_victory.json` | Trophy with confetti celebration | Duel Result Screen (win) | 3s one-shot |
| `lottie_defeat.json` | Sad face / try again animation | Duel Result Screen (loss) | 2s one-shot |
| `lottie_draw.json` | Handshake / tie animation | Duel Result Screen (draw) | 2s one-shot |
| `lottie_correct_answer.json` | Checkmark with sparkle particles | Duel Screen (correct answer reveal) | 1s one-shot |
| `lottie_wrong_answer.json` | X mark with shake effect | Duel Screen (wrong answer reveal) | 1s one-shot |
| `lottie_streak_fire.json` | Fire animation, growing intensity | Duel Screen (streak indicator) | Loop |
| `lottie_countdown_321.json` | 3-2-1-GO countdown sequence | Matchmaking Screen (pre-duel) | 3s one-shot |
| `lottie_empty_state.json` | Empty box / ghost for empty lists | Quiz List, Duel History (no data) | Loop |
| `lottie_loading.json` | Branded loading spinner | General loading states | Loop |

**Recommended specs:**
- Format: Lottie JSON (exported from After Effects via Bodymovin)
- Canvas size: 300x300px for icons, 500x500px for full-screen
- Frame rate: 30fps
- Colors: Match app palette (Primary #6C5CE7, Secondary #FF6B6B, Success #00B894, Error #E17055)

---

## Sound Effects

Location: `app/src/main/res/raw/`

| File Name | Description | Trigger |
|-----------|-------------|---------|
| `sfx_tap.mp3` | Soft click / tap sound | Answer option selected |
| `sfx_correct.mp3` | Positive chime / ding | Correct answer revealed |
| `sfx_wrong.mp3` | Low buzz / error tone | Wrong answer revealed |
| `sfx_timer_tick.mp3` | Clock tick sound | Timer < 5 seconds remaining |
| `sfx_timer_urgent.mp3` | Urgent beeping | Timer < 3 seconds remaining |
| `sfx_match_found.mp3` | Dramatic whoosh / impact | Opponent found in matchmaking |
| `sfx_victory.mp3` | Triumphant fanfare | Duel result: victory |
| `sfx_defeat.mp3` | Somber descending tone | Duel result: defeat |
| `sfx_score_up.mp3` | Point pop / coin ding | Score increment animation |
| `sfx_streak.mp3` | Fire crackle / power-up | Streak milestone (3, 5, 7+) |
| `sfx_question_in.mp3` | Swoosh / slide sound | New question sliding in |
| `sfx_countdown.mp3` | Countdown beep (3 beeps) | Pre-duel 3-2-1 countdown |

**Recommended specs:**
- Format: MP3 or OGG (MP3 preferred for size)
- Duration: 0.3-2 seconds (keep short for responsiveness)
- Sample rate: 44.1kHz
- Bitrate: 128kbps
- Mono channel (stereo unnecessary for UI sounds)

---

## App Icons

Location: `app/src/main/res/mipmap-*/` and `app/src/main/res/drawable/`

### Launcher Icon
| Resource | Size | Description |
|----------|------|-------------|
| `ic_launcher.webp` (mipmap-mdpi) | 48x48 | App launcher icon |
| `ic_launcher.webp` (mipmap-hdpi) | 72x72 | App launcher icon |
| `ic_launcher.webp` (mipmap-xhdpi) | 96x96 | App launcher icon |
| `ic_launcher.webp` (mipmap-xxhdpi) | 144x144 | App launcher icon |
| `ic_launcher.webp` (mipmap-xxxhdpi) | 192x192 | App launcher icon |
| `ic_launcher_round.webp` | Same sizes | Round variant |
| `ic_launcher_foreground.webp` | 108x108dp (432px) | Adaptive icon foreground |
| `ic_launcher_background.xml` | N/A | Adaptive icon background color |

### Custom Vector Icons (drawable/)
Most icons use **Material Icons Extended** (already in dependencies). Custom vectors needed only for:

| Resource | Description | Where Used |
|----------|-------------|------------|
| `ic_app_logo.xml` | App logo as vector drawable | Splash, Login, About |
| `ic_ai_badge.xml` | Small robot icon for AI opponents | Matchmaking, History |
| `ic_streak_fire.xml` | Custom fire icon for streaks | Duel Screen |
| `ic_rating_star.xml` | Star icon for rating badge | Profile, Home |

**Material Icons already available (no assets needed):**
- Home: `Icons.Rounded.Home`
- Leaderboard: `Icons.Rounded.EmojiEvents`
- Profile: `Icons.Rounded.Person`
- Search: `Icons.Rounded.Search`
- Back: `Icons.AutoMirrored.Rounded.ArrowBack`
- Edit: `Icons.Rounded.Edit`
- Logout: `Icons.Rounded.Logout`
- Play: `Icons.Rounded.PlayArrow`
- Timer: `Icons.Rounded.Timer`
- Check: `Icons.Rounded.Check`
- Close: `Icons.Rounded.Close`

---

## Images

Location: `app/src/main/res/drawable/` or loaded via URL (Coil)

### Static Images (bundled in app)
| Resource | Size | Description |
|----------|------|-------------|
| `img_logo_splash.webp` | 256x256 | High-res logo for splash (if not using Lottie) |
| `img_guest_login_bg.webp` | 1080x1920 | Background illustration for login screen |
| `img_avatar_default.webp` | 128x128 | Default user avatar placeholder |
| `img_empty_quiz.webp` | 200x200 | Placeholder for quizzes without thumbnails |

### Predefined Avatars (for profile edit)
| Resource | Size | Description |
|----------|------|-------------|
| `img_avatar_01.webp` through `img_avatar_12.webp` | 128x128 each | 12 predefined avatar options |

**Suggested avatar themes:** Astronaut, Ninja, Robot, Cat, Dog, Dragon, Wizard, Pirate, Alien, Knight, Scientist, Superhero

### AI Opponent Personas
| Resource | Size | Description |
|----------|------|-------------|
| `img_ai_quizbot.webp` | 128x128 | AI persona: QuizBot (default) |
| `img_ai_brainwave.webp` | 128x128 | AI persona: BrainWave (medium difficulty) |
| `img_ai_speedyq.webp` | 128x128 | AI persona: SpeedyQ (easy) |
| `img_ai_professor.webp` | 128x128 | AI persona: Professor X (hard) |
| `img_ai_quizwhiz.webp` | 128x128 | AI persona: Quiz Whiz (variable) |

### Leaderboard Rank Badges
| Resource | Size | Description |
|----------|------|-------------|
| `img_rank_gold.webp` | 64x64 | 1st place badge |
| `img_rank_silver.webp` | 64x64 | 2nd place badge |
| `img_rank_bronze.webp` | 64x64 | 3rd place badge |

**Recommended image specs:**
- Format: WebP (best compression for Android)
- Use vector drawables (XML) wherever possible for scalability
- Minimum resolution: 2x the listed sizes for high-DPI devices

---

## Summary

| Category | Count | Priority |
|----------|-------|----------|
| Lottie Animations | 12 | Medium (app works with Compose fallback animations) |
| Sound Effects | 12 | Low (app works silently, add later) |
| App Icons | 7+ variants | High (required for app to run) |
| Custom Vector Icons | 4 | Medium (can use Material Icons as fallback) |
| Static Images | 4 | Medium (can use colored placeholders) |
| Avatars | 12 | Low (can use generated initials) |
| AI Personas | 5 | Low (can use default avatar) |
| Rank Badges | 3 | Low (can use text/emoji) |

**Total unique assets: ~52 files**

For MVP launch, only the **App Icons** are truly required. Everything else can use programmatic fallbacks (Compose animations, colored shapes, Material Icons, initial-based avatars).
