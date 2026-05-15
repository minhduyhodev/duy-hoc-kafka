const { useEffect, useState } = React;

const API_BASE = "/api/weather";

function App() {
  const [form, setForm] = useState({
    city: "Ho Chi Minh",
    requestedBy: "",
    recipientEmail: "",
  });
  const [status, setStatus] = useState(null);
  const [results, setResults] = useState([]);
  const [health, setHealth] = useState("Checking service...");
  const [submitState, setSubmitState] = useState("idle");
  const [feedback, setFeedback] = useState("");
  const [errors, setErrors] = useState([]);
  const [lastUpdated, setLastUpdated] = useState("");

  useEffect(() => {
    loadDashboard();

    const intervalId = window.setInterval(() => {
      loadDashboard();
    }, 5000);

    return () => window.clearInterval(intervalId);
  }, []);

  async function loadDashboard() {
    try {
      const [healthResponse, statusResponse, resultsResponse] = await Promise.all([
        fetch(`${API_BASE}/health`),
        fetch(`${API_BASE}/status`),
        fetch(`${API_BASE}/results?limit=6`),
      ]);

      const healthText = await healthResponse.text();
      const statusJson = await statusResponse.json();
      const resultsJson = await resultsResponse.json();

      setHealth(healthText);
      setStatus(statusJson);
      setResults(Array.isArray(resultsJson) ? resultsJson : []);
      setLastUpdated(formatClock(new Date().toISOString()));
    } catch (error) {
      setHealth("Backend is not available yet");
      setFeedback("Unable to refresh live data. Start Spring Boot and Kafka, then reload.");
    }
  }

  function handleInputChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitState("loading");
    setFeedback("");
    setErrors([]);

    const payload = {
      city: form.city.trim(),
      requestedBy: form.requestedBy.trim(),
      recipientEmail: form.recipientEmail.trim() || null,
    };

    try {
      const response = await fetch(`${API_BASE}/forecast`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (!response.ok) {
        setSubmitState("error");
        setErrors(Array.isArray(data.errors) ? data.errors : []);
        setFeedback(data.message || "Failed to send weather request.");
        return;
      }

      setSubmitState("success");
      setFeedback(`Forecast request for ${data.city} was pushed to Kafka successfully.`);
      setForm((current) => ({
        ...current,
        requestedBy: "",
      }));

      window.setTimeout(() => {
        loadDashboard();
      }, 1200);
    } catch (error) {
      setSubmitState("error");
      setFeedback("Cannot reach the backend. Make sure Spring Boot is running on port 8080.");
    }
  }

  return (
    <main className="page-shell">
      <section className="hero-panel">
        <div className="hero-copy">
          <p className="eyebrow">React interface for your Spring Boot + Kafka demo</p>
          <h1>Weather Kafka Control Room</h1>
          <p className="hero-text">
            Send forecast jobs, watch the processing pipeline, and review the latest live
            weather results in one place.
          </p>

          <div className="status-strip">
            <StatusPill label="Service" value={health} tone={health.includes("running") ? "good" : "muted"} />
            <StatusPill
              label="Mail"
              value={status?.mailEnabled ? "Enabled" : "Disabled"}
              tone={status?.mailEnabled ? "accent" : "muted"}
            />
            <StatusPill
              label="Recent results"
              value={String(status?.recentResults ?? 0)}
              tone="good"
            />
          </div>
        </div>

        <div className="pipeline-card">
          <p className="card-kicker">Kafka flow</p>
          <div className="pipeline-step">1. React form sends POST request to `/api/weather/forecast`</div>
          <div className="pipeline-step">2. Producer pushes request into `weather-request-topic`</div>
          <div className="pipeline-step">3. Consumer resolves the city and fetches live weather from Open-Meteo</div>
          <div className="pipeline-step">4. Result goes to `weather-result-topic` and appears below</div>
          <p className="pipeline-foot">
            Last sync: <strong>{lastUpdated || "Waiting for first refresh"}</strong>
          </p>
        </div>
      </section>

      <section className="content-grid">
        <section className="glass-card form-card">
          <div className="section-heading">
            <p className="card-kicker">New forecast request</p>
            <h2>Queue a weather job</h2>
          </div>

          <form className="forecast-form" onSubmit={handleSubmit}>
            <label>
              City
              <input
                name="city"
                value={form.city}
                onChange={handleInputChange}
                placeholder="Da Nang"
                autoComplete="off"
              />
            </label>

            <label>
              Requested by
              <input
                name="requestedBy"
                value={form.requestedBy}
                onChange={handleInputChange}
                placeholder="Your name"
                autoComplete="off"
              />
            </label>

            <label>
              Recipient email
              <input
                name="recipientEmail"
                type="email"
                value={form.recipientEmail}
                onChange={handleInputChange}
                placeholder="Optional email for notification"
                autoComplete="off"
              />
            </label>

            <button className={`submit-button ${submitState}`} type="submit" disabled={submitState === "loading"}>
              {submitState === "loading" ? "Sending to Kafka..." : "Send forecast request"}
            </button>
          </form>

          <div className={`feedback-box ${submitState}`}>
            <p>{feedback || "Submit a request to see the queue response here."}</p>
            {errors.length > 0 && (
              <ul className="error-list">
                {errors.map((error) => (
                  <li key={error}>{error}</li>
                ))}
              </ul>
            )}
          </div>
        </section>

        <section className="glass-card insight-card">
          <div className="section-heading">
            <p className="card-kicker">System snapshot</p>
            <h2>Runtime details</h2>
          </div>

          <div className="metrics-grid">
            <MetricCard label="Application" value={status?.application || "Loading"} />
            <MetricCard label="Weather API" value={status?.weatherProvider || "Loading"} />
            <MetricCard label="Request topic" value={status?.requestTopic || "Loading"} />
            <MetricCard label="Result topic" value={status?.resultTopic || "Loading"} />
            <MetricCard label="Mail mode" value={status?.mailEnabled ? "Enabled" : "Disabled"} />
          </div>
        </section>
      </section>

      <section className="glass-card results-card">
        <div className="section-heading">
          <p className="card-kicker">Latest forecasts</p>
          <h2>Processed results from Kafka</h2>
        </div>

        {results.length === 0 ? (
          <div className="empty-state">
            <p>No forecast results yet. Start Kafka, submit a request, and the cards will appear here.</p>
          </div>
        ) : (
          <div className="results-grid">
            {results.map((result, index) => (
              <article className="result-tile" key={`${result.city}-${result.forecastTime}-${index}`}>
                <div className="result-head">
                  <div>
                    <p className="result-city">{result.city}</p>
                    <p className="result-requester">Requested by {result.requestedBy}</p>
                  </div>
                  <div className="temperature-badge">{result.temperature} deg C</div>
                </div>

                <p className="weather-status">{result.status}</p>
                <p className="result-time">Forecast time: {formatDateTime(result.forecastTime)}</p>
                <p className="result-email">
                  Email: {result.recipientEmail && result.recipientEmail.trim() ? result.recipientEmail : "Not provided"}
                </p>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

function MetricCard({ label, value }) {
  return (
    <div className="metric-card">
      <p>{label}</p>
      <strong>{value}</strong>
    </div>
  );
}

function StatusPill({ label, value, tone }) {
  return (
    <div className={`status-pill ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function formatDateTime(value) {
  if (!value) {
    return "Pending";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "medium",
  }).format(new Date(value));
}

function formatClock(value) {
  return new Intl.DateTimeFormat("vi-VN", {
    timeStyle: "medium",
  }).format(new Date(value));
}

const rootElement = document.getElementById("root");

if (ReactDOM.createRoot) {
  ReactDOM.createRoot(rootElement).render(<App />);
} else {
  ReactDOM.render(<App />, rootElement);
}
