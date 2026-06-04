"""Export Lab4_22521258_22520953.docx to Lab4_22521258_22520953.pdf."""
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT_STEM = "Lab4_22521258_22520953"
DOCX = ROOT / f"{REPORT_STEM}.docx"
PDF = ROOT / f"{REPORT_STEM}.pdf"


def export_with_docx2pdf() -> bool:
    try:
        from docx2pdf import convert
        convert(str(DOCX), str(PDF))
        return PDF.exists()
    except Exception as e:
        print(f"docx2pdf failed: {e}")
        return False


def export_with_libreoffice() -> bool:
    soffice = shutil.which("soffice") or shutil.which("libreoffice")
    if not soffice:
        for candidate in [
            r"C:\Program Files\LibreOffice\program\soffice.exe",
            r"C:\Program Files (x86)\LibreOffice\program\soffice.exe",
        ]:
            if Path(candidate).exists():
                soffice = candidate
                break
    if not soffice:
        return False
    try:
        subprocess.run(
            [soffice, "--headless", "--convert-to", "pdf", "--outdir", str(ROOT), str(DOCX)],
            check=True,
            capture_output=True,
            timeout=120,
        )
        return PDF.exists()
    except Exception as e:
        print(f"LibreOffice failed: {e}")
        return False


def export_with_word_com() -> bool:
    ps = f"""
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {{
  $doc = $word.Documents.Open("{DOCX.resolve()}")
  $doc.SaveAs([ref] "{PDF.resolve()}", [ref] 17)
  $doc.Close()
}} finally {{
  $word.Quit()
  [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
}}
"""
    try:
        subprocess.run(
            ["powershell", "-NoProfile", "-Command", ps],
            check=True,
            capture_output=True,
            timeout=120,
        )
        return PDF.exists()
    except Exception as e:
        print(f"Word COM failed: {e}")
        return False


def main():
    if not DOCX.exists():
        print(f"Error: {DOCX} not found. Run generate_report.py first.")
        sys.exit(1)

    for name, fn in [
        ("docx2pdf", export_with_docx2pdf),
        ("LibreOffice", export_with_libreoffice),
        ("Word COM", export_with_word_com),
    ]:
        print(f"Trying {name}...")
        if fn():
            print(f"PDF saved to {PDF}")
            return

    print("All PDF export methods failed.")
    sys.exit(1)


if __name__ == "__main__":
    main()
