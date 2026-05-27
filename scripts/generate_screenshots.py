"""Generate demonstration screenshots from game drawable assets."""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable"
OUT = ROOT / "docs" / "screenshots"

W, H = 1080, 1920


def load(name: str, size=None) -> Image.Image:
    path = DRAWABLE / name
    img = Image.open(path).convert("RGBA")
    if size:
        img = img.resize(size, Image.Resampling.LANCZOS)
    return img


def draw_hud(draw, score=30, high=120, lives=3, level=2):
    try:
        font_lg = ImageFont.truetype("arial.ttf", 48)
        font_md = ImageFont.truetype("arial.ttf", 38)
    except OSError:
        font_lg = ImageFont.load_default()
        font_md = ImageFont.load_default()
    draw.text((40, 60), f"Score: {score}", fill="white", font=font_lg)
    draw.text((40, 120), f"High Score: {high}", fill="white", font=font_md)
    draw.text((40, 175), f"Lives: {lives}", fill="white", font=font_md)
    draw.text((40, 230), f"Level: {level}", fill="white", font=font_md)


def draw_health_bar(draw, x, y, w, ratio):
    h = 8
    draw.rectangle([x, y, x + w, y + h], fill="red")
    draw.rectangle([x, y, x + w * ratio, y + h], fill="lime")


def gameplay_screen():
    bg = load("galaxy_background.jpg", (W, H))
    canvas = bg.copy()
    draw = ImageDraw.Draw(canvas)

    boss = load("alian.png", (200, 200))
    canvas.paste(boss, (W // 2 - 100, 60), boss)

    enemies = [
        (load("rocket.png", (100, 100)), 180, 400),
        (load("rocket_2.png", (100, 100)), 480, 550),
        (load("alian.png", (80, 80)), 720, 700),
        (load("token_red_emovebg.png", (100, 100)), 400, 850),
    ]
    for img, x, y in enemies:
        canvas.paste(img, (x, y), img)
        draw_health_bar(draw, x, y - 14, img.width, 0.6)

    ship = load("rocket_2.png", (90, 90)).rotate(180)
    canvas.paste(ship, (W // 2 - 45, H - 200), ship)

    for bx in [W // 2 - 10, W // 2 - 35, W // 2 + 25]:
        draw.rectangle([bx, H - 350, bx + 20, H - 310], fill="yellow")

    draw_hud(draw, score=30, high=120, lives=3, level=2)
    return canvas


def hud_screen():
    img = gameplay_screen()
    draw = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype("arial.ttf", 36)
    except OSError:
        font = ImageFont.load_default()
    draw.rectangle([20, 40, 420, 280], outline="cyan", width=3)
    draw.text((30, 290), "HUD: Score, High Score, Lives, Bullet Level", fill="cyan", font=font)
    return img


def game_over_screen():
    img = gameplay_screen()
    draw = ImageDraw.Draw(img)
    try:
        font_lg = ImageFont.truetype("arial.ttf", 90)
        font_sm = ImageFont.truetype("arial.ttf", 40)
    except OSError:
        font_lg = ImageFont.load_default()
        font_sm = ImageFont.load_default()
    draw.text((W // 2 - 280, H // 2 - 80), "Game Over", fill="red", font=font_lg)
    draw.text((W // 2 - 200, H // 2 + 30), "Tap to restart", fill="white", font=font_sm)
    draw_hud(draw, score=50, high=120, lives=0, level=3)
    return img


def boss_screen():
    img = gameplay_screen()
    draw = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype("arial.ttf", 36)
    except OSError:
        font = ImageFont.load_default()
    draw.rectangle([W // 2 - 120, 40, W // 2 + 120, 280], outline="yellow", width=3)
    draw.text((30, 290), "Boss alien spawns minions from center (Exercise 7)", fill="yellow", font=font)
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    screens = [
        ("01_gameplay.png", gameplay_screen),
        ("02_hud.png", hud_screen),
        ("03_boss_minions.png", boss_screen),
        ("04_game_over.png", game_over_screen),
    ]
    for name, fn in screens:
        path = OUT / name
        fn().save(path)
        print(f"Saved {path}")


if __name__ == "__main__":
    main()
