import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import ketchLogo from "./assets/ketch.svg";
import "./App.css";

function App() {
  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
        <a href="https://www.ketch.com/" target="_blank">
          <img src={ketchLogo} className="logo" alt="React logo" />
        </a>
      </div>
      <h1>
        Vite + React +{" "}
        <a
          href="https://www.ketch.com/"
          target="_blank"
          style={{ color: "rgb(123, 61, 233)", fontWeight: "bold" }}
        >
          Ketch
        </a>
      </h1>
      <div className="card">
        <div
          style={{
            display: "flex",
            gap: 8,
            justifyContent: "center",
          }}
        >
          <button onClick={() => (window as any).ketch("showConsent")}>
            Show Consent
          </button>
          <button onClick={() => (window as any).ketch("showPreferences")}>
            Show Preferences
          </button>
        </div>
      </div>
    </>
  );
}

export default App;
