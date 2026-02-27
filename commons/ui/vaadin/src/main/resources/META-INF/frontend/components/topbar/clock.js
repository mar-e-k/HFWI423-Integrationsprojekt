export function initClock(element) {
    if (!element) return;

    function updateClock() {
        const now = new Date();
        element.textContent = now.toLocaleString('de-DE', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    }

    updateClock();
    element._intervalId = setInterval(updateClock, 1000);
}