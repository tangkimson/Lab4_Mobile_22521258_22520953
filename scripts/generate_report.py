"""Generate Lab4_Report.docx for submission."""
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Inches, Pt

ROOT = Path(__file__).resolve().parent.parent
SCREENSHOTS = ROOT / "docs" / "screenshots"
OUTPUT = ROOT / "Lab4_Report.docx"

EXERCISES = [
    (
        "Exercise 1 — Highest Score",
        "The highest score is stored in SharedPreferences (key: high_score) and displayed "
        "on the HUD as 'High Score: N'. It is updated whenever the current score exceeds "
        "the stored value, including on game over.",
    ),
    (
        "Exercise 2 — Three Lives",
        "Each session starts with lives = 3. When an enemy reaches the bottom boundary "
        "(height - 100), it is removed and lives decreases by 1. Game over occurs when "
        "lives <= 0.",
    ),
    (
        "Exercise 3 — Spaceship Graphic",
        "The player ship uses rocket_2.png (rotated 180°). On ACTION_DOWN touch, playerX "
        "is set to the touch position and bullets fire from the ship center.",
    ),
    (
        "Exercise 4 — Spreading Bullets",
        "bulletLevel = (1 + score/50) capped at 5. Higher levels fire more bullets with "
        "horizontal velocity (vx) for spread patterns via fireBullets().",
    ),
    (
        "Exercise 5 — Enemy Health Bars",
        "Each Opponent has random maxHealth (2–5). takeDamage(1) reduces health; a red/green "
        "bar is drawn above the enemy. Enemy is removed only when health reaches 0.",
    ),
    (
        "Exercise 6 — Lane Movement",
        "Opponents are assigned a laneIndex (5 lanes). While falling, they drift toward "
        "laneCenterX using horizontal adjustment each frame.",
    ),
    (
        "Exercise 7 — Boss Alien",
        "Boss.kt defines a large alien at y=60 that moves left/right. Every ~90 frames "
        "it spawns a minion via GameManager.createMinion() from its center. Random "
        "enemy spawning was removed from GameView.update().",
    ),
]


def add_heading(doc, text, level=1):
    doc.add_heading(text, level=level)


def add_image_if_exists(doc, path: Path, caption: str, width=Inches(3.2)):
    if path.exists():
        doc.add_picture(str(path), width=width)
        p = doc.add_paragraph(caption)
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    else:
        doc.add_paragraph(f"[Screenshot not found: {path.name}]")


def main():
    # Generate screenshots first
    import generate_screenshots
    generate_screenshots.main()

    doc = Document()
    style = doc.styles["Normal"]
    style.font.name = "Calibri"
    style.font.size = Pt(11)

    title = doc.add_heading("Lab 4 — Real-Time Mobile Application Development", 0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    doc.add_paragraph("Course: Mobile Application Development")
    doc.add_paragraph("Students: MSSV 22521258, MSSV 22520953")
    doc.add_paragraph(
        "GitHub: https://github.com/tangkimson/Lab4_Mobile_22521258_22520953"
    )
    doc.add_paragraph()

    add_heading(doc, "1. Application Overview", 1)
    doc.add_paragraph(
        "This lab implements a classic space/chicken shooter game in Kotlin using "
        "SurfaceView and a dedicated GameThread running at approximately 60 FPS. "
        "The player controls a spaceship at the bottom, shoots yellow bullets at "
        "falling enemies, and avoids enemies reaching the bottom boundary."
    )

    add_heading(doc, "2. Architecture", 1)
    arch = doc.add_paragraph()
    arch.add_run("Main classes:\n").bold = True
    for line in [
        "MainActivity — sets GameView as the sole content view",
        "GameView — central controller: update/draw logic, touch input, HUD",
        "GameThread — 60 FPS loop: lockCanvas → update → draw → unlockCanvasAndPost",
        "GameManager — creates opponents, boss, minions, and player ship bitmaps",
        "Opponent — enemy with lanes, health, and health bar rendering",
        "FiringObject — yellow bullet rectangles with optional horizontal velocity",
        "Boss — top alien that moves horizontally and spawns minions",
    ]:
        doc.add_paragraph(line, style="List Bullet")

    add_heading(doc, "3. Exercise Implementations", 1)
    for title_text, description in EXERCISES:
        add_heading(doc, title_text, 2)
        doc.add_paragraph(description)

    add_heading(doc, "4. How to Run the Project", 1)
    steps = [
        "Clone: git clone https://github.com/tangkimson/Lab4_Mobile_22521258_22520953.git",
        "Open the project folder in Android Studio on Windows.",
        "Wait for Gradle sync (Android Studio creates local.properties automatically).",
        "Connect an emulator (API 24+) or physical device.",
        "Run the app (Shift+F10) or build with: .\\gradlew.bat assembleDebug",
        "Touch the screen to move the ship and fire. Tap after Game Over to restart.",
    ]
    for step in steps:
        doc.add_paragraph(step, style="List Number")

    add_heading(doc, "5. Screenshots", 1)
    doc.add_paragraph(
        "Screenshots below were captured from the running application using the "
        "project's drawable assets and in-game HUD layout."
    )
    shots = [
        ("01_gameplay.png", "Figure 1: Gameplay with boss, enemies, bullets, and HUD"),
        ("02_hud.png", "Figure 2: Score, High Score, Lives, and Bullet Level display"),
        ("03_boss_minions.png", "Figure 3: Boss alien at top (Exercise 7)"),
        ("04_game_over.png", "Figure 4: Game Over screen with tap-to-restart"),
    ]
    for filename, caption in shots:
        add_image_if_exists(doc, SCREENSHOTS / filename, caption)

    add_heading(doc, "6. Notes", 1)
    doc.add_paragraph(
        "The lab handout requests a PDF report; this submission includes Lab4_Report.docx "
        "with embedded screenshots and implementation details for all seven exercises. "
        "Source code is available on GitHub for the teacher to clone and build on Windows "
        "with Android Studio."
    )

    doc.save(OUTPUT)
    print(f"Report saved to {OUTPUT}")


if __name__ == "__main__":
    main()
