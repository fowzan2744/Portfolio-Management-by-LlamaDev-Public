import { Sun, Moon } from "lucide-react";
import { useEffect, useState } from "react";
import { Toggle } from "./toggle";

export default function ThemeToggle() {
  const [isDark, setIsDark] = useState<boolean>(() => {
    if (typeof window === "undefined") return false;
    const stored = localStorage.getItem("theme");
    if (stored) return stored === "dark";
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  });

  useEffect(() => {
    const root = document.documentElement;
    if (isDark) root.classList.add("dark");
    else root.classList.remove("dark");
    try {
      localStorage.setItem("theme", isDark ? "dark" : "light");
    } catch (e) {
      // ignore
    }
  }, [isDark]);

  return (
    <Toggle
      aria-label="Toggle theme"
      pressed={isDark}
      onPressedChange={(val: boolean) => setIsDark(val)}
      size="sm"
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </Toggle>
  );
}
