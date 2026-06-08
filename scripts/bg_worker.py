import os
import sys
import time
from firebase_admin import credentials, initialize_app, storage
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
    print("Iniciando bg_worker...")
    
    # Initialize Firebase Admin if not already initialized
    try:
        # Assumes serviceAccountKey.json is in the same directory or project root
        cred_path = os.path.join(os.path.dirname(__file__), '..', 'serviceAccountKey.json')
        if not os.path.exists(cred_path):
            cred_path = 'serviceAccountKey.json'
            
        cred = credentials.Certificate(cred_path)
        initialize_app(cred, {
            'storageBucket': 'almacen-inteligente-2f515.appspot.com'
        })
    except Exception as e:
        print(f"Firebase Admin ya inicializado o error: {e}")

    bucket = storage.bucket()
    blobs = bucket.list_blobs(prefix="items/")
    
    session = new_session("u2net") # Use high quality model
    processed_count = 0

    for blob in blobs:
        # Check custom metadata
        metadata = blob.metadata
        if metadata and metadata.get("bg_processed") == "true":
            continue
            
        # It might be a placeholder or non-image
        if not blob.name.lower().endswith(('.png', '.jpg', '.jpeg', '.webp')):
            continue

        print(f"Procesando nueva imagen: {blob.name}")
        
        try:
            # Download image into memory
            image_bytes = blob.download_as_bytes()
            input_image = Image.open(io.BytesIO(image_bytes))
            
            # Apply AI Background Removal
            # alpha_matting=True gives softer, more natural edges
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
            
            # Convert back to bytes (PNG format to preserve transparency)
            output_buffer = io.BytesIO()
            cropped_image.save(output_buffer, format="PNG", optimize=True)
            output_bytes = output_buffer.getvalue()
            
            # Upload back to Firebase Storage, overwriting the original file
            # Set content_type to image/png so browsers render transparency
            new_metadata = {"bg_processed": "true"}
            blob.metadata = new_metadata
            
            # We must re-upload with the new metadata
            blob.upload_from_string(
                output_bytes, 
                content_type="image/png"
            )
            
            # Update the custom metadata explicitly after upload
            blob.metadata = new_metadata
            blob.patch()
            
            print(f"Imagen {blob.name} procesada y guardada exitosamente.")
            processed_count += 1
            
        except Exception as e:
            print(f"Error procesando {blob.name}: {e}")

    if processed_count == 0:
        print("No se encontraron imágenes nuevas para procesar.")
    else:
        print(f"Se procesaron {processed_count} imágenes.")

if __name__ == "__main__":
    process_images()
