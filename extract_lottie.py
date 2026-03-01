import zipfile
import shutil
import os

lottie_file = r"C:\Users\heman\Downloads\Checkmark.lottie"
out_json = r"C:\Users\heman\Documents\code\duelUpAndroid\app\src\main\res\raw\lottie_correct_answer.json"

try:
    with zipfile.ZipFile(lottie_file, 'r') as zip_ref:
        json_filename = None
        for name in zip_ref.namelist():
            if name.endswith('.json') and not name.endswith('manifest.json'):
                json_filename = name
                break
        
        if json_filename:
            with zip_ref.open(json_filename) as source, open(out_json, "wb") as target:
                shutil.copyfileobj(source, target)
            print(f"Successfully extracted {json_filename} to {out_json}")
        else:
            print("Could not find a valid animation .json file inside the .lottie archive.")
except zipfile.BadZipFile:
    print("Error: The file is not a valid zip archive. It might be corrupt.")
except FileNotFoundError:
    print(f"Error: Could not find the file {lottie_file}")
except Exception as e:
    print(f"An error occurred: {e}")
