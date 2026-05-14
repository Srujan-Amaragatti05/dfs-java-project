import os
import random
import subprocess
from datetime import datetime, timedelta
from pathlib import Path


PROJECT_PATH = Path(__file__).parent

# Timeline
START_DATE = datetime(2026, 2, 23)
END_DATE = datetime(2026, 3, 19)

# Commit messages
COMMIT_MESSAGES = [
    "Initial project structure",
    "Refactor DFS traversal logic",
    "Improve graph utility methods",
    "Add comments and cleanup",
    "Optimize adjacency list handling",
    "Fix edge-case traversal bug",
    "Update README documentation",
    "Improve input validation",
    "Minor code cleanup",
    "Improve naming consistency",
    "Add sample test cases",
    "Refactor helper methods",
    "Improve console output",
    "Update project documentation",
    "Code formatting improvements",
]


# Files that can be modified randomly
TARGET_FILES = []

for ext in ["*.java", "*.md", "*.txt"]:
    TARGET_FILES.extend(PROJECT_PATH.rglob(ext))


if not TARGET_FILES:
    raise Exception("No files found to modify.")


current_date = START_DATE

while current_date <= END_DATE:

    # Randomly decide whether to commit on this day
    should_commit = random.choice([True, False, True])

    if should_commit:

        # 1–4 commits per selected day
        commits_today = random.randint(1, 4)

        for _ in range(commits_today):

            file_to_modify = random.choice(TARGET_FILES)

            with open(file_to_modify, "a", encoding="utf-8") as f:
                f.write(f"\n// update: {datetime.now()}\n")

            commit_time = current_date.replace(
                hour=random.randint(9, 23),
                minute=random.randint(0, 59),
                second=random.randint(0, 59),
            )

            formatted_date = commit_time.strftime("%Y-%m-%d %H:%M:%S")

            env = os.environ.copy()
            env["GIT_AUTHOR_DATE"] = formatted_date
            env["GIT_COMMITTER_DATE"] = formatted_date

            subprocess.run(["git", "add", "."], check=True)

            status = subprocess.check_output(
                ["git", "status", "--porcelain"]
            ).decode().strip()
            
            if status:
                subprocess.run(
                    [
                        "git",
                        "commit",
                        "-m",
                        random.choice(COMMIT_MESSAGES),
                    ],
                    check=True,
                    env=env,
                )
            
                print(f"Committed: {formatted_date}")
            
            else:
                print("Skipped empty commit")
            
                current_date += timedelta(days=1)


print("\nDone generating commit timeline.")
'''
Control Panel
→ Credential Manager
→ Windows Credentials

rmdir /s /q .git     

git init
git branch -M main
git remote add origin repo_url

git config --global user.email

python commit_generator.py

git push -u origin main
'''