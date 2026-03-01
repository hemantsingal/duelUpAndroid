import os
import lottie
from lottie.objects import Animation, Layer
from lottie.objects.properties import MultiDimensional, Keyframe
from lottie.objects.layers import NullLayer

svg_path = r"C:\Users\heman\Downloads\-a-clean--minimalist-2d-flat-vector-logo-for-a-tri.svg"
out_path = r"C:\Users\heman\Documents\code\duelUpAndroid\app\src\main\res\raw\lottie_splash_logo.json"

# Find importer and import SVG
importer = lottie.importers.importers.get_from_extension("svg")
animation = importer.process(svg_path)

animation.frame_rate = 30
animation.in_point = 0
animation.out_point = 45 # 1.5s * 30fps

width = animation.width if hasattr(animation, 'width') else 1024
height = animation.height if hasattr(animation, 'height') else 1024
center_x = width / 2
center_y = height / 2

# Create a master Null layer to control scaling from the center
master_null = NullLayer()
master_null.name = "Scale Controller"
master_null.transform.position.value = [center_x, center_y]
master_null.transform.anchor_point.value = [center_x, center_y]

# Add pulse animation to the master null
master_null.transform.scale.add_keyframe(0, [100, 100])
master_null.transform.scale.add_keyframe(22, [110, 110]) # Pulse up
master_null.transform.scale.add_keyframe(45, [100, 100])

animation.add_layer(master_null)

# Parent all original SVG layers to the master null
if hasattr(animation, 'layers'):
    for layer in animation.layers:
        if getattr(layer, "name", "") != "Scale Controller":
            # Just set the parent.
            layer.parent_index = master_null.index

# Export
exporter = lottie.exporters.exporters.get_from_extension("json")
exporter.process(animation, out_path)
print(f"Exported animated Lottie to {out_path} using a Null parent.")
