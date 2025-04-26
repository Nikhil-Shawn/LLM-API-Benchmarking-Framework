import { useState } from "react";
import { generateOutputs } from "../services/api";
import { autoEvaluateOutput, getCriteria, getTotalScore } from "../utils/evaluation";
import ModelCard from "./ModelCard";

const codeModels = [
  { id: "starcoder", name: "StarCoder (HuggingFace)" },
  { id: "mistral", name: "Mistral AI" },
  { id: "gemini", name: "Gemini 1.5 Pro (Google)" }
];

const imageModels = [
  { id: "stableDiffusion", name: "Stable Diffusion XL Lightning (ByteDance)" },
  { id: "dalle", name: "DALL-E (OpenAI)" },
  { id: "gemini", name: "Gemini (Google)" } 
];

export default function LLMComparisonApp() {
  const [useCase, setUseCase] = useState("code");
  const [prompt, setPrompt] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [evaluation, setEvaluation] = useState({ scores: { A: {}, B: {}, C: {} } });
  const [showCriteria, setShowCriteria] = useState(false);

  const [selectedModels, setSelectedModels] = useState({
    code: { modelA: "starcoder", modelB: "mistral", modelC: "gemini" },
    image: { modelA: "stableDiffusion", modelB: "dalle", modelC: "gemini" }
  });

  const currentModels = useCase === "code" ? codeModels : imageModels;

  const handleModelChange = (position, modelId) => {
    setSelectedModels(prev => ({
      ...prev,
      [useCase]: { ...prev[useCase], [position]: modelId }
    }));
  };

  const getModelName = (position) => {
    const modelId = selectedModels[useCase][position];
    return currentModels.find(m => m.id === modelId)?.name || "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const models = [
        selectedModels[useCase].modelA,
        selectedModels[useCase].modelB,
        selectedModels[useCase].modelC
      ];

      const data = await generateOutputs(useCase, prompt, models);

      const formattedResults = {
        modelA: { name: getModelName("modelA"), output: data.results[0].output },
        modelB: { name: getModelName("modelB"), output: data.results[1].output },
        modelC: { name: getModelName("modelC"), output: data.results[2].output }
      };

      setResults(formattedResults);

      const autoScores = {
        A: autoEvaluateOutput(formattedResults.modelA.output, useCase, prompt),
        B: autoEvaluateOutput(formattedResults.modelB.output, useCase, prompt),
        C: autoEvaluateOutput(formattedResults.modelC.output, useCase, prompt)
      };

      setEvaluation({ scores: autoScores });
    } catch (error) {
      console.error("Error generating results:", error);
      alert("Failed to generate results. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="app-content">
      <div className="app-header">
        <h1 className="app-title">LLM Comparison App</h1>
        <p className="app-description">Compare outputs from different LLM providers for code and image generation</p>

        <form onSubmit={handleSubmit} className="app-form">
          <div className="form-group">
            <label className="form-label">Select Use Case:</label>
            <div className="button-group">
              {["code", "image"].map(type => (
                <button
                  type="button"
                  key={type}
                  onClick={() => setUseCase(type)}
                  className={`use-case-button ${useCase === type ? "use-case-button-active" : ""}`}
                >
                  {type === "code" ? "Code Generation" : "Image Generation"}
                </button>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Select Models to Compare:</label>
            <div className="models-grid">
              {["modelA", "modelB", "modelC"].map(position => (
                <div className="model-select-box" key={position}>
                  <h3 className="model-select-title">Model {position.slice(-1)}:</h3>
                  <select
                    value={selectedModels[useCase][position]}
                    onChange={(e) => handleModelChange(position, e.target.value)}
                    className="model-select"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
              ))}
            </div>
          </div>

          <div className="form-group">
            <div className="prompt-header">
              <label className="form-label">Enter your prompt for {useCase === "code" ? "code" : "image"} generation:</label>
              <button
                type="button"
                onClick={() => setShowCriteria(!showCriteria)}
                className="criteria-toggle"
              >
                {showCriteria ? "Hide evaluation criteria" : "Show evaluation criteria"}
              </button>
            </div>
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              className="prompt-input"
              rows="4"
              required
            />
          </div>

          {showCriteria && (
            <div className="criteria-box">
              <h3 className="criteria-title">Evaluation Criteria:</h3>
              <ul className="criteria-list-detailed">
                {getCriteria(useCase).map(c => (
                  <li key={c.key}><strong>{c.label}</strong></li>
                ))}
              </ul>
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading || !prompt.trim()}
            className={`submit-button ${isLoading || !prompt.trim() ? "submit-button-disabled" : ""}`}
          >
            {isLoading ? "Processing..." : "Generate and Compare"}
          </button>
        </form>
      </div>

      {results && (
        <div className="results-container">
          <h2 className="results-title">Comparison Results:</h2>
          <div className="results-grid">
            {["A", "B", "C"].map(key => (
              <ModelCard
                key={key}
                modelKey={key}
                modelData={results[`model${key}`]}
                scores={evaluation.scores[key]}
                useCase={useCase}
              />
            ))}
          </div>

          <div className="evaluation-footer">
            <div className="evaluation-summary">
              {(() => {
                const scores = ["A", "B", "C"].map(m => ({
                  model: m,
                  score: getTotalScore(evaluation.scores[m]),
                  name: results[`model${m}`].name
                })).sort((a, b) => b.score - a.score);

                if (scores[0].score === scores[1].score && scores[1].score === scores[2].score) {
                  return "All models scored equally";
                } else if (scores[0].score === scores[1].score) {
                  return `${scores[0].name} and ${scores[1].name} tied for highest score`;
                } else {
                  return `${scores[0].name} scored highest with ${scores[0].score} points`;
                }
              })()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
