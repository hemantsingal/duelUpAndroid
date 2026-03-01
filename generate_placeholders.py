import os
from PIL import Image, ImageDraw, ImageFont

res_dir = r"C:\Users\heman\Documents\code\duelUpAndroid\app\src\main\res\drawable"
os.makedirs(res_dir, exist_ok=True)

placeholders = [
    # filename, width, height, background color, text
    ("img_empty_quiz.png", 200, 200, (200, 200, 200), "Empty\nQuiz"),
    ("img_ai_speedyq.png", 128, 128, (255, 107, 107), "SpeedyQ\n(AI)"),
    ("img_ai_quizwhiz.png", 128, 128, (108, 92, 231), "Whiz\n(AI)"),
    ("img_rank_gold.png", 64, 64, (255, 215, 0), "1st"),
    ("img_rank_silver.png", 64, 64, (192, 192, 192), "2nd"),
    ("img_rank_bronze.png", 64, 64, (205, 127, 50), "3rd")
]

for filename, w, h, color, text in placeholders:
    img = Image.new('RGB', (w, h), color=color)
    d = ImageDraw.Draw(img)
    
    # Try to calculate basic text centering
    # (Since we might not have a TTF font loaded, we'll just use the default bitmap font
    # and approximate the center)
    text_x = w / 4
    text_y = h / 2 - 10
    
    # Draw simple lines/shapes to make it obvious it's a placeholder
    d.rectangle([5, 5, w-5, h-5], outline=(255, 255, 255), width=2)
    d.text((text_x, text_y), text, fill=(255, 255, 255))
    
    path = os.path.join(res_dir, filename)
    img.save(path)
    print(f"Generated placeholder: {filename}")

print("\nDone! All 6 remaining placeholder images have been created.")
