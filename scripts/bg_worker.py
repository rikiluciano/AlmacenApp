import os
import sys
import time
import base64
from firebase_admin import credentials, initialize_app, firestore
from rembg import remove, new_session
from PIL import Image
import io

def crop_transparent(image):
    """Crops the transparent margins of the image."""
    bbox = image.getbbox()
    if bbox:
        return image.crop(bbox)
    return image

def process_images():
    print("Iniciando bg_worker para procesar Base64 desde Firestore...")
    
    # Initialize Firebase Admin
    try:
        cred_path = os.path.join(os.path.dirname(__file__), '..', 'serviceAccountKey.json')
        if not os.path.exists(cred_path):
            cred_path = 'serviceAccountKey.json'
            
        cred = credentials.Certificate(cred_path)
        initialize_app(cred)
    except Exception as e:
        print(f"Firebase Admin ya inicializado o error: {e}")

    db = firestore.client()
    items_ref = db.collection("items")
    docs = items_ref.stream()
    
    session = new_session("u2net")
    processed_count = 0

    for doc in docs:
        data = doc.to_dict()
        photo_path = data.get("photoPath")
        bg_processed = data.get("bg_processed")

        # Skip if already processed or if there's no photo or if it's not a base64 string
        if bg_processed == True:
            continue
            
        if not photo_path or not photo_path.startswith("data:image/"):
            continue

        print(f"Procesando nueva imagen para el ítem: {data.get('name', doc.id)}")
        
        try:
            # Extract base64 part
            header, encoded = photo_path.split(",", 1)
            image_bytes = base64.b64decode(encoded)
            
            input_image = Image.open(io.BytesIO(image_bytes))
            
            # Apply AI Background Removal
            output_image = remove(
                input_image, 
                session=session,
                alpha_matting=True,
                alpha_matting_foreground_threshold=240,
                alpha_matting_background_threshold=10,
                alpha_matting_erode_size=10
            )
            
            # Crop transparent empty space
            cropped_image = crop_transparent(output_image)
            
            # Convert back to base64 (PNG format to preserve transparency)
            output_buffer = io.BytesIO()
            cropped_image.save(output_buffer, format="PNG", optimize=True)
            output_bytes = output_buffer.getvalue()
            
            new_base64 = base64.b64encode(output_bytes).decode('utf-8')
            new_photo_path = f"data:image/png;base64,{new_base64}"
            
            # Update the document in Firestore
            items_ref.document(doc.id).update({
                "photoPath": new_photo_path,
                "bg_processed": True
            })
            
            print(f"Imagen del ítem {doc.id} procesada y guardada exitosamente.")
            processed_count += 1
            
        except Exception as e:
            print(f"Error procesando ítem {doc.id}: {e}")

    if processed_count == 0:
        print("No se encontraron imágenes nuevas para procesar.")
    else:
        print(f"Se procesaron {processed_count} imágenes.")

if __name__ == "__main__":
    process_images()
