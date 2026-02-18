export function copyToClipboard(text) {
    if (!navigator.clipboard) {
        console.warn("Clipboard API not available");
        return false;
    }
    navigator.clipboard.writeText(text)
        .then(() => console.log("Copied to clipboard"))
        .catch(err => console.error("Failed to copy:", err));
}