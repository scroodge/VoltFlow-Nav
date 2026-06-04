#!/usr/bin/env python3
"""Apply the Yandex patches to a freshly-decoded OpenBYD smali tree.

Run by build.sh after `apktool d`. Idempotent: re-running on an already-patched
tree is a no-op. Every edit asserts it matched exactly once, so any upstream
change (new apktool version / new OpenBYD build) fails loudly instead of
silently producing a broken APK.
"""
import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
DECODED = HERE / "decoded"
SVC = DECODED / "smali/com/sr/openbyd/services/MapNotificationListenerService.smali"
YBLOG_SRC = HERE / "patches/YBLog.smali"
YBLOG_DST = DECODED / "smali/com/sr/openbyd/YBLog.smali"


def edit(text, old, new, label, marker=None):
    """Replace `old` with `new`, requiring exactly one match.

    `marker` is a substring unique to the patched state; if already present the
    edit is skipped (idempotent). Defaults to `new`, but must be given explicitly
    when `old` is a prefix/substring of `new` (otherwise the check is unreliable).
    """
    if (marker or new) in text:
        print(f"  = {label}: already applied")
        return text
    n = text.count(old)
    if n != 1:
        sys.exit(f"  ! {label}: expected exactly 1 match for anchor, found {n}. "
                 f"Upstream smali changed — update apply_patches.py.")
    print(f"  + {label}")
    return text.replace(old, new)


def main():
    if not SVC.exists():
        sys.exit(f"Decoded tree not found: {SVC}\nRun build.sh (it decodes first).")

    # 1. install the file-logger class
    shutil.copyfile(YBLOG_SRC, YBLOG_DST)
    print(f"  + YBLog.smali -> {YBLOG_DST.relative_to(DECODED)}")

    s = SVC.read_text()

    # 2. bump <clinit> register count so we have v2 for the third package string
    s = edit(s,
             ".method static constructor <clinit>()V\n    .locals 2",
             ".method static constructor <clinit>()V\n    .locals 3",
             "clinit .locals 2 -> 3")

    # 3. add Yandex to the MAP_PACKAGES whitelist array
    s = edit(s,
             '    const-string v1, "app.revanced.android.apps.maps"\n',
             '    const-string v1, "app.revanced.android.apps.maps"\n\n'
             '    const-string v2, "ru.yandex.yandexnavi"\n',
             "whitelist: declare yandex string",
             marker='const-string v2, "ru.yandex.yandexnavi"')
    s = edit(s,
             "filled-new-array {v0, v1}, [Ljava/lang/String;",
             "filled-new-array {v0, v1, v2}, [Ljava/lang/String;",
             "whitelist: 2-elem -> 3-elem array")

    # 4. inject the file-log call where title/text/subText are resolved
    #    (v1=this/Context, v6=title, v9=text, v8=subText)
    s = edit(s,
             "    :cond_a\n    :goto_0\n"
             "    sget-object v10, Ljava/util/Locale;->ROOT:Ljava/util/Locale;",
             "    :cond_a\n    :goto_0\n"
             "    invoke-static {v1, v6, v9, v8}, "
             "Lcom/sr/openbyd/YBLog;->w(Landroid/content/Context;"
             "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V\n\n"
             "    sget-object v10, Ljava/util/Locale;->ROOT:Ljava/util/Locale;",
             "inject YBLog.w() call")

    SVC.write_text(s)
    print("Patches applied.")


if __name__ == "__main__":
    main()
