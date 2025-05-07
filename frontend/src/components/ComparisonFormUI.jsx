import React from "react";
import ModelCard from "./ModelCard";

export default function ComparisonFormUI({
  useCase = "image",         
  prompt = "",
  setPrompt = () => {},
  isLoading = false,
  onSubmit = () => {},
  modelOptions = [],
  selectedModels = { modelA: "", modelB: "", modelC: "" },
  onModelChange = () => {},
  showCriteria = false,
  toggleCriteria = () => {},
  criteria = [],
  results = null,
  scores = {},                
  getTotalScore = () => 0
}) {
  return (
    <div className="app-content">
      {/* Form Card */}
      <div className="app-header">
        <h1 className="app-title">LLM Comparison App</h1>
        <p className="app-description">Compare outputs for code and image generation</p>
        <form onSubmit={onSubmit} className="app-form">
          {/* Model selectors */}
          <div className="form-group">
            <label>Select Models:</label>
            <div className="models-grid">
              {["modelA","modelB","modelC"].map(slot => (
                <div key={slot} className="model-select-box">
                  <h3>Model {slot.slice(-1)}</h3>
                  <select
                    value={selectedModels[slot] || ""}
                    onChange={e => onModelChange(slot, e.target.value)}
                    className="model-select"
                  >
                    <option value="" disabled>Select a model</option>
                    {modelOptions.map(m => (
                      <option key={m.id} value={m.id}>{m.name}</option>
                    ))}
                  </select>
                </div>
              ))}
            </div>
          </div>

          {/* Prompt + criteria toggle */}
          <div className="form-group">
            <div className="prompt-header">
              <label>Enter prompt for {useCase}:</label>
              <button type="button" onClick={toggleCriteria} className="criteria-toggle">
                {showCriteria ? "Hide criteria" : "Show criteria"}
              </button>
            </div>
            <textarea
              className="prompt-input"
              rows={4}
              required
              value={prompt}
              onChange={e => setPrompt(e.target.value)}
            />
          </div>

          {showCriteria && (
            <div className="criteria-box">
              <h3>Evaluation Criteria:</h3>
              <ul className="criteria-list-detailed">
                {criteria.map(c => (
                  <li key={c.key}><strong>{c.label}</strong></li>
                ))}
              </ul>
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading || !prompt.trim()}
            className={`submit-button ${(isLoading || !prompt.trim()) ? "submit-button-disabled" : ""}`}
          >
            {isLoading ? "Processing…" : "Generate & Compare"}
          </button>
        </form>
      </div>

      {/* Results Card */}
      {results && (
        <div className="results-container">
          <div className="results-grid">
            {Object.entries(results).map(([slot, data]) => {
              const letter = slot.slice(-1);
              const perScores = scores[letter] || {};
              const total = getTotalScore(perScores);

              if (useCase === "code") {
                return (
                  <div key={slot} className="model-card">
                    <div className="model-card-header">
                      <h3>{data.name}</h3>
                      <span className="score-chip">Score: {total}</span>
                    </div>
                    <pre className="code-output">{data.output}</pre>
                    <div className="scores-criteria">
                      <h4>Scores by Criteria:</h4>
                      <ul>
                        {criteria.map(c => (
                          <li key={c.key}>• {c.label}: {perScores[c.key] ?? '-'}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                );
              }

              return (
                <ModelCard
                  key={letter}
                  modelKey={letter}
                  modelData={data}
                  scores={perScores}
                />
              );
            })}
          </div>

          <div className="evaluation-summary">
            {(() => {
              const ranked = Object.entries(results).map(([slot, data]) => {
                const letter = slot.slice(-1);
                return { name: data.name, score: getTotalScore(scores[letter] || {}) };
              }).sort((a, b) => b.score - a.score);

              if (ranked[0].score === ranked[2]?.score) return "All tied";
              if (ranked[0].score === ranked[1].score)
                return `${ranked[0].name} & ${ranked[1].name} tied`;
              return `${ranked[0].name} wins with ${ranked[0].score}`;
            })()}
          </div>
        </div>
      )}
    </div>
  );
}
