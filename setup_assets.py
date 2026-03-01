import os
import base64

res_dir = r"C:\Users\heman\Documents\code\duelUpAndroid\app\src\main\res"

# Define directories
dirs = [
    os.path.join(res_dir, "raw"),
    os.path.join(res_dir, "drawable"),
    os.path.join(res_dir, "mipmap-mdpi"),
    os.path.join(res_dir, "mipmap-hdpi"),
    os.path.join(res_dir, "mipmap-xhdpi"),
    os.path.join(res_dir, "mipmap-xxhdpi"),
    os.path.join(res_dir, "mipmap-xxxhdpi")
]

for d in dirs:
    os.makedirs(d, exist_ok=True)

# Lottie JSON files
lottie_files = [
    "lottie_splash_logo.json", "lottie_matchmaking_search.json", "lottie_matchmaking_found.json",
    "lottie_victory.json", "lottie_defeat.json", "lottie_draw.json", "lottie_correct_answer.json",
    "lottie_wrong_answer.json", "lottie_streak_fire.json", "lottie_countdown_321.json",
    "lottie_empty_state.json", "lottie_loading.json"
]

blank_lottie = '{"v": "5.5.2", "fr": 30, "ip": 0, "op": 60, "w": 500, "h": 500, "nm": "Blank", "ddd": 0, "assets": [], "layers": []}'

raw_dir = os.path.join(res_dir, "raw")

for f in lottie_files:
    path = os.path.join(raw_dir, f)
    with open(path, "w", encoding="utf-8") as file:
        file.write(blank_lottie)

# Sound effects files
sfx_files = [
    "sfx_tap.mp3", "sfx_correct.mp3", "sfx_wrong.mp3", "sfx_timer_tick.mp3",
    "sfx_timer_urgent.mp3", "sfx_match_found.mp3", "sfx_victory.mp3", "sfx_defeat.mp3",
    "sfx_score_up.mp3", "sfx_streak.mp3", "sfx_question_in.mp3", "sfx_countdown.mp3"
]

silent_mp3_b64 = "SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//OEAAAAAAAAAAAAAAAAAAAAAAAASW5mbwAAAA8AAAAEAAABIAD+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v7+/v+cAAAAAExhdmM1OC4xMzQAAAAAAAAAAAAAAAAkAEQAAAAAAAABIAAAAAAA//MUZAAAAAGkAAAAAAAAA0gAAAAATEFNRTMuMTAwqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq//MUZCQAAAGkAAAAAAAAA0gAAAAAxVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV//MUZFAAAAGkAAAAAAAAA0gAAAAAxVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV//MUZHgAAAGkAAAAAAAAA0gAAAAAxVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV"

silent_mp3_bytes = base64.b64decode(silent_mp3_b64 + "=" * ((4 - len(silent_mp3_b64) % 4) % 4))

for f in sfx_files:
    path = os.path.join(raw_dir, f)
    with open(path, "wb") as file:
        file.write(silent_mp3_bytes)

print("Directories and placeholders set up successfully!")
