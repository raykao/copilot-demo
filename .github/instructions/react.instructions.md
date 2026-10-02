---
applyTo: "client/**"
---

# React client conventions

- Function components in `src/components/`, one per file, with a one-line JSDoc comment describing the component.
- Add every endpoint to the `api` object in `src/api/client.js`. Components never call `fetch` directly.
- Use `async` functions inside `useEffect`. Catch `ApiError` and render it with `ErrorBanner`.
- Format money with `formatMoney()` from `src/format.js`.
- Keep styling in `src/styles.css` and reuse the `.panel`, `.primary` and table styles.
- The client must work against either backend, so rely only on what `docs/API.md` documents.
