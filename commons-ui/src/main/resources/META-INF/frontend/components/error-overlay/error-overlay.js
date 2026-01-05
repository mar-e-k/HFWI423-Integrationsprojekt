export function autoCloseDialog(dialogElement, durationMs) {
    if (!dialogElement) return;

    requestAnimationFrame(() => {
        const timeoutId = setTimeout(() => {
            if (dialogElement.opened) {
                dialogElement.opened = false;
            }
        }, durationMs);

        dialogElement.addEventListener("opened-changed", (event) => {
            if (!event.detail.value) {
                clearTimeout(timeoutId);
            }
        });
    });
}