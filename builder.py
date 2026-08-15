import os
import sys
from groq import Groq

# Groq client initialization with API key
client = Groq(api_key="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")

def generate_app_code(prompt):
    system_prompt = """
    You are an expert Android developer. 
    Write a complete, working 'MainActivity.java' code for an Android app based on the user's request.
    Use package name 'com.mycompany.calc'.
    Do not use lambda expressions if it causes compatibility issues, stick to standard Java 8/17 syntax.
    Return ONLY the raw Java code inside standard text, no markdown blocks if possible, or standard markdown.
    """
    
    response = client.chat.completions.create(
        model="llama-3.3-70b-versatile",  # Groq ka sabse best aur latest free coding model
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": prompt}
        ],
        temperature=0.7
    )
    
    return response.choices[0].message.content

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python builder.py 'App description yahan likhein'")
        sys.exit(1)
        
    user_prompt = sys.argv[1]
    print(f"[*] Generating code for: '{user_prompt}'...")
    
    java_code = generate_app_code(user_prompt)
    
    # Clean up markdown code blocks if AI returned them
    if "```java" in java_code:
        java_code = java_code.split("```java")[1].split("```")[0].strip()
    elif "```" in java_code:
        java_code = java_code.split("```")[1].split("```")[0].strip()
        
    # File path jahan code save hoga
    file_path = "app/src/main/java/com/mycompany/calc/MainActivity.java"
    
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(java_code)
        
    print("[*] Code generated and saved successfully!")
    
    # Auto Git Commit & Push
    print("[*] Pushing to GitHub...")
    os.system("git add .")
    os.system('git commit -m "Auto-generated app update via AI command"')
    os.system("git push origin main")
    print("[*] Done! Check your GitHub Actions to download the APK.")
