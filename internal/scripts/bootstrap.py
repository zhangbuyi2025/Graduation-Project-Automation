#!/usr/bin/env python
"""环境自举：检测并自动补齐本 skill 运行所需的最小依赖。

用法（在任意目录执行）：
    python "<SKILL_DIR>/internal/scripts/bootstrap.py"          # 检测 + 自动安装
    python "<SKILL_DIR>/internal/scripts/bootstrap.py" --check  # 只检测，不安装
    python "<SKILL_DIR>/internal/scripts/bootstrap.py" --mirror # 直接用国内镜像安装

安装策略：先走官方源；失败自动改用清华 / 阿里云镜像，无需用户干预。
退出码：0 = 全部就绪；78 = 仍有依赖缺失（附手动处理指引）。
输出末行为 ``BOOTSTRAP: OK`` 时，才可进入后续阶段。
"""

from __future__ import annotations

import argparse
import importlib.util
import shutil
import subprocess
import sys
from pathlib import Path

SKILL_DIR = Path(__file__).resolve().parent.parent.parent
REQUIREMENTS = SKILL_DIR / "requirements.txt"
DEPS_DIR = SKILL_DIR / "internal" / "deps"
DOCX_MODULE = DEPS_DIR / "node_modules" / "docx"

# (import 名, pip 包名)：只列流水线真正会 import 的依赖，避免装一堆用不上的东西
PY_MODULES = [
    ("pptx", "python-pptx"),
    ("PIL", "Pillow"),
    ("flask", "flask"),
    ("yaml", "PyYAML"),
    ("markitdown", "markitdown"),
    ("docx", "python-docx"),
]

# 官方源优先，失败后依次尝试国内镜像
PIP_INDEXES = [
    "https://pypi.tuna.tsinghua.edu.cn/simple",
    "https://mirrors.aliyun.com/pypi/simple/",
]
NPM_REGISTRIES = ["https://registry.npmmirror.com"]


def _run(cmd: list[str], timeout: int = 1800) -> int:
    print("$ " + " ".join(cmd), flush=True)
    try:
        return subprocess.call(cmd, timeout=timeout)
    except FileNotFoundError:
        print("  -> command not found: " + cmd[0], flush=True)
        return 127
    except subprocess.TimeoutExpired:
        print("  -> timeout after %ss" % timeout, flush=True)
        return 124


def missing_python() -> list[str]:
    missing = []
    for module, package in PY_MODULES:
        try:
            if importlib.util.find_spec(module) is None:
                missing.append(package)
        except (ImportError, ValueError):
            missing.append(package)
    return missing


def has_pip() -> bool:
    return _run([sys.executable, "-m", "pip", "--version"], timeout=60) == 0


def install_python(mirror_first: bool) -> bool:
    if not REQUIREMENTS.exists():
        print("[python] requirements.txt not found: %s" % REQUIREMENTS, flush=True)
        return False
    if not has_pip():
        print("[python] pip missing, running ensurepip ...", flush=True)
        _run([sys.executable, "-m", "ensurepip", "--upgrade"], timeout=300)

    base = [sys.executable, "-m", "pip", "install", "-r", str(REQUIREMENTS),
            "--disable-pip-version-check", "--quiet"]
    indexes = ([None] + PIP_INDEXES) if mirror_first is False else (PIP_INDEXES + [None])
    for index in indexes:
        cmd = list(base) if index is None else base + ["-i", index]
        print("[python] index = %s" % (index or "default"), flush=True)
        if _run(cmd) == 0 and not missing_python():
            return True
    # 权限不足时退回用户目录再试一次（仍带镜像回退）
    print("[python] retry with --user ...", flush=True)
    for index in indexes:
        cmd = list(base) + ["--user"] if index is None else base + ["--user", "-i", index]
        if _run(cmd) == 0 and not missing_python():
            return True
    return False


def install_node(mirror_first: bool) -> bool:
    npm = shutil.which("npm") or shutil.which("npm.cmd")
    if not npm:
        print("[node] npm not found on PATH.", flush=True)
        return False
    base = [npm, "install", "--prefix", str(DEPS_DIR), "--no-audit", "--no-fund"]
    registries = ([None] + NPM_REGISTRIES) if mirror_first is False else (NPM_REGISTRIES + [None])
    for registry in registries:
        cmd = list(base) if registry is None else base + ["--registry", registry]
        print("[node] registry = %s" % (registry or "default"), flush=True)
        if _run(cmd) == 0 and DOCX_MODULE.exists():
            return True
    return False


def main() -> int:
    parser = argparse.ArgumentParser(description="Bootstrap skill dependencies.")
    parser.add_argument("--check", action="store_true", help="only check, do not install")
    parser.add_argument("--mirror", action="store_true", help="prefer CN mirrors")
    args = parser.parse_args()

    print("SKILL_DIR = %s" % SKILL_DIR, flush=True)
    problems: list[str] = []

    # 1) Python 解释器版本
    if sys.version_info < (3, 10):
        problems.append("Python >= 3.10 required, found %s" % sys.version.split()[0])

    # 2) Python 依赖
    missing = missing_python()
    if missing:
        print("[python] missing: %s" % ", ".join(missing), flush=True)
        if args.check:
            problems.append("python packages missing: " + ", ".join(missing))
        else:
            print("[python] installing minimal requirements ...", flush=True)
            if not install_python(args.mirror):
                problems.append("pip install failed: " + ", ".join(missing_python() or missing))
    else:
        print("[python] ok", flush=True)

    # 3) Node 依赖（SRS DOCX 生成器用的 docx-js）
    if DOCX_MODULE.exists():
        print("[node] ok (%s)" % DOCX_MODULE, flush=True)
    else:
        print("[node] %s missing" % DOCX_MODULE, flush=True)
        if args.check:
            problems.append("node deps missing: internal/deps/node_modules/docx")
        else:
            print("[node] installing into internal/deps ...", flush=True)
            if not install_node(args.mirror):
                problems.append("npm install failed for internal/deps")

    if problems:
        print("", flush=True)
        print("BOOTSTRAP: BLOCKED", flush=True)
        for item in problems:
            print("  - " + item, flush=True)
        print("Manual fix:", flush=True)
        print('  python -m pip install -r "%s" -i https://pypi.tuna.tsinghua.edu.cn/simple' % REQUIREMENTS, flush=True)
        print('  npm install --prefix "%s" --registry https://registry.npmmirror.com' % DEPS_DIR, flush=True)
        print("  (offline? copy site-packages / node_modules from a machine that has them)", flush=True)
        return 78

    print("", flush=True)
    print("BOOTSTRAP: OK", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
