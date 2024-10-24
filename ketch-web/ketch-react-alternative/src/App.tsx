import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import ketchLogo from "./assets/ketch.svg";
import "./App.css";
import { useEffect } from "react";

function App() {
  useEffect(() => {
    const scriptId = "ketch-tag"; // Unique ID for the script

    // Check if the script is already present
    if (!document.getElementById(scriptId)) {
      const script = document.createElement("script");
      script.id = scriptId; // Set the ID to prevent duplicate inserts
      script.type = "text/javascript";

      // Set the inline script content
      script.innerHTML = `
      !(function () {
        (window.semaphore = window.semaphore || []),
          (window.ketch = function () {
            window.semaphore.push(arguments);
          });
        var e = new URLSearchParams(document.location.search),
          o = e.has("property") ? e.get("property") : "website_smart_tag",
          n = document.createElement("script");
        (n.type = "text/javascript"),
          (n.src =
            "https://global.ketchcdn.com/web/v3/config/ketch_samples/".concat(
              o,
              "/boot.js"
            )),
          (n.defer = n.async = !0),
          document.getElementsByTagName("head")[0].appendChild(n);
      })();
    `;

      document.body.appendChild(script); // Insert the script into the body
    }
  }, []);

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
