/** Shows an API error, including the request ID to search for in logs/app.log. */
export default function ErrorBanner({ error }) {
  if (!error) return null;
  return (
    <div className="error-banner" role="alert">
      <strong>{error.code ?? 'ERROR'}</strong> {error.message}
      {error.requestId && (
        <span className="request-id">
          request <code>{error.requestId}</code>
        </span>
      )}
    </div>
  );
}
