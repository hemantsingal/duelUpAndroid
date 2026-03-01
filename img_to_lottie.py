import sys
import base64
import json
import os
from PIL import Image

def image_to_floating_lottie(image_path, output_path):
    # Read the image
    img = Image.open(image_path)
    width, height = img.size

    # Convert image to base64
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    
    ext = os.path.splitext(image_path)[1].lower()
    mime_type = "image/png"
    if ext in ['.jpg', '.jpeg']:
        mime_type = "image/jpeg"

    # Define the Lottie JSON structure manually for a simple floating image
    # We create a precomp that contains the image, and then animate the precomp's position
    
    center_x = width / 2
    center_y = height / 2

    lottie_data = {
        "v": "5.5.2",
        "fr": 30,
        "ip": 0,
        "op": 60, # 2 seconds loop
        "w": width,
        "h": height,
        "nm": "Floating Image",
        "ddd": 0,
        "assets": [
            {
                "id": "image_0",
                "w": width,
                "h": height,
                "u": "",
                "p": f"data:{mime_type};base64,{encoded_string}",
                "e": 1
            }
        ],
        "layers": [
            {
                "ddd": 0,
                "ind": 1,
                "ty": 2, # Image layer
                "nm": "Image",
                "refId": "image_0",
                "sr": 1,
                "ks": {
                    "o": {"a": 0, "k": 100, "ix": 11},
                    "r": {"a": 0, "k": 0, "ix": 10},
                    "p": {
                        "a": 1, # Animated position
                        "k": [
                            {"i": {"x": 0.5, "y": 0.5}, "o": {"x": 0.5, "y": 0.5}, "t": 0, "s": [center_x, center_y, 0]},
                            {"i": {"x": 0.5, "y": 0.5}, "o": {"x": 0.5, "y": 0.5}, "t": 30, "s": [center_x, center_y - 20, 0]}, # Float up 20px
                            {"i": {"x": 0.5, "y": 0.5}, "o": {"x": 0.5, "y": 0.5}, "t": 60, "s": [center_x, center_y, 0]} # Down
                        ],
                        "ix": 2
                    },
                    "a": {"a": 0, "k": [center_x, center_y, 0], "ix": 1},
                    "s": {"a": 0, "k": [100, 100, 100], "ix": 6}
                },
                "ao": 0,
                "ip": 0,
                "op": 60,
                "st": 0,
                "bm": 0
            }
        ],
        "markers": []
    }

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(lottie_data, f)
    
    print(f"Successfully converted {image_path} to an animated Lottie JSON at {output_path}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python img_to_lottie.py <path_to_image> <output_json_path>")
    else:
        in_path = sys.argv[1]
        out_path = sys.argv[2]
        image_to_floating_lottie(in_path, out_path)
