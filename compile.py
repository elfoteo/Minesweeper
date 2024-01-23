import os
import subprocess
import hashlib

def hash_string(input_string):
    # Create a new SHA-256 hash object
    sha256_hash = hashlib.sha256()

    # Update the hash object with the bytes of the input string
    sha256_hash.update(input_string.encode('utf-8'))

    # Get the hexadecimal representation of the hash
    hashed_string = sha256_hash.hexdigest()

    return hashed_string


def read_file(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            file_content = file.read()
        return file_content
    except FileNotFoundError:
        return f"File not found: {file_path}"
    except Exception as e:
        return f"Error reading file: {e}"

def write_to_file(file_path, content):
    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(content)
        print(f"Content successfully written to '{file_path}'.")
    except Exception as e:
        print(f"Error writing to file: {e}")


def compile_java_files(source_dir, output_dir, cache_dir, classpath):
    # Walk through the source directory
    for root, dirs, files in os.walk(source_dir):
        for file in files:
            # Check if the file is a Java source file
            if file.endswith(".java") and not ("compiler-cache" in root):
                source_path = os.path.join(root, file)
                package_path = os.path.relpath(os.path.dirname(source_path), source_dir)
                class_path = os.path.join(output_dir, package_path)

                # Create the output directory if it doesn't exist
                os.makedirs(class_path, exist_ok=True)

                # Check if the file is already compiled (in the cache)
                cache_file_name = os.path.splitext(file)[0] + ".hash"
                cache_path = os.path.join(cache_dir, package_path, cache_file_name)
                source_content = read_file(source_path)
                if os.path.exists(cache_path) and hash_string(source_content) == read_file(cache_path):
                    print(f"Skipping compilation of {source_path} (already in cache)")
                    continue

                # Compile the Java file with classpath dependencies
                subprocess.run(["javac", "-d", output_dir, "-cp", f".;{classpath}", source_path])

                # Update the cache with the compiled Java file
                os.makedirs(os.path.dirname(cache_path), exist_ok=True)
                write_to_file(cache_path, hash_string(source_content))
                print(f"Compiled {source_path} and updated cache.")


if __name__ == "__main__":
    source_directory = "."
    output_directory = "out"
    cache_directory = "compiler-cache"
    classpath_dependencies = "lanterna-3.1.1.jar;json-java.jar"
    main_class = "Program.java"

    # Create the compiler cache directory if it doesn't exist
    os.makedirs(cache_directory, exist_ok=True)

    compile_java_files(source_directory, output_directory, cache_directory, classpath_dependencies)
    print("Java files compiled successfully.")

    print(f"Running {main_class}")
    subprocess.run(["javaw", "-cp", f".;{classpath_dependencies};{output_directory}", main_class])
