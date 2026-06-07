import os
import ftplib

# FTP Credentials
FTP_HOST = "ftpupload.net"
FTP_USER = "if0_42105996"
FTP_PASS = "rikiluciano123"

def get_files_to_upload(directory):
    file_paths = []
    for root, directories, files in os.walk(directory):
        for filename in files:
            filepath = os.path.join(root, filename)
            file_paths.append(filepath)
    return file_paths

def deploy():
    print(f"Connecting to {FTP_HOST}...")
    ftp = ftplib.FTP(FTP_HOST)
    ftp.login(user=FTP_USER, passwd=FTP_PASS)
    print("Connected successfully.")

    # El directorio public_html o htdocs en InfinityFree
    try:
        ftp.cwd('htdocs')
    except:
        print("htdocs not found. Assuming root is public.")

    # Subir backend
    backend_files = get_files_to_upload('backend')
    for file in backend_files:
        filename = os.path.basename(file)
        print(f"Uploading {filename}...")
        with open(file, 'rb') as f:
            ftp.storbinary(f'STOR {filename}', f)

    # Subir web
    web_files = get_files_to_upload('web')
    for file in web_files:
        rel_path = os.path.relpath(file, 'web')
        # Crear directorios si no existen
        remote_dir = os.path.dirname(rel_path).replace("\\", "/")
        if remote_dir and remote_dir != ".":
            # Very basic dir creation handling
            dirs = remote_dir.split('/')
            current = ""
            for d in dirs:
                current = f"{current}/{d}" if current else d
                try:
                    ftp.mkd(current)
                except:
                    pass
        
        remote_path = rel_path.replace("\\", "/")
        print(f"Uploading {remote_path}...")
        with open(file, 'rb') as f:
            ftp.storbinary(f'STOR {remote_path}', f)

    ftp.quit()
    print("Deployment complete!")

if __name__ == "__main__":
    deploy()
