export function toggleTheme() {
    const themeList = document.documentElement.classList;
    const currentTheme = document.documentElement.getAttribute('theme');

    if (currentTheme === 'dark') {
        themeList.remove('dark');
        document.documentElement.removeAttribute('theme');
        localStorage.setItem('theme', 'light');
    } else {
        themeList.add('dark');
        document.documentElement.setAttribute('theme', 'dark');
        localStorage.setItem('theme', 'dark');
    }
}