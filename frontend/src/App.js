import { useState } from "react";
import './App.css';


const API_BASE_URL = "http://localhost:8081";

const autoEvaluateOutput = (output, useCase) => {
  const criteria = useCase === "code"
    ? ["correctness", "efficiency", "readability", "robustness", "documentation"]
    : ["relevance", "quality", "creativity", "consistency", "composition"];

  const scores = {};
  criteria.forEach((criterion) => {
    // Implement your evaluation logic here.
    // For demonstration, assigning a random score between 6 and 10.
    scores[criterion] = Math.floor(Math.random() * 5) + 6;
  });

  return scores;
};

// Available model options for each use case - reduced to 3 models
const codeModels = [
  { id: "starcoder", name: "StarCoder (HuggingFace)" },
  { id: "gpt35", name: "GPT-3.5 (OpenAI)" },
  { id: "codellama", name: "CodeLlama (Meta)" }
];

const imageModels = [
  { id: "stablediffusion", name: "Stable Diffusion (Stability AI)" },
  { id: "dalle", name: "DALL-E (OpenAI)" },
  { id: "midjourney", name: "Midjourney API" }
];

export default function LLMComparisonApp() {
  const [useCase, setUseCase] = useState("code");
  const [prompt, setPrompt] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [evaluation, setEvaluation] = useState({
    scores: { A: {}, B: {}, C: {} },
    feedback: ""
  });
  const [showCriteria, setShowCriteria] = useState(false);
  
  // Model selection state
  const [selectedModels, setSelectedModels] = useState({
    code: { 
      modelA: "starcoder", 
      modelB: "gpt35", 
      modelC: "codellama" 
    },
    image: { 
      modelA: "stablediffusion", 
      modelB: "dalle", 
      modelC: "midjourney" 
    }
  });
  
  const currentModels = useCase === "code" ? codeModels : imageModels;
  
  // Handle model selection change
  const handleModelChange = (position, modelId) => {
    setSelectedModels(prev => ({
      ...prev,
      [useCase]: {
        ...prev[useCase],
        [position]: modelId
      }
    }));
  };
  
  // Get current model names
  const getModelName = (position) => {
    const modelId = selectedModels[useCase][position];
    return currentModels.find(m => m.id === modelId)?.name || "";
  };
  
  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setEvaluation({ scores: { A: {}, B: {}, C: {} }, feedback: "" });
  
    try {
      const endpoint = useCase === "code"
        ? `${API_BASE_URL}/api/generate-code`
        : `${API_BASE_URL}/api/generate-image`;
  
      const models = [
        selectedModels[useCase].modelA,
        selectedModels[useCase].modelB,
        selectedModels[useCase].modelC
      ];
  
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ prompt, models })
      });
  
      if (!response.ok) {
        throw new Error("Failed to generate results");
      }
  
      const data = await response.json();
  
      const formattedResults = {
        modelA: {
          name: getModelName("modelA"),
          output: data.results[0].output
        },
        modelB: {
          name: getModelName("modelB"),
          output: data.results[1].output
        },
        modelC: {
          name: getModelName("modelC"),
          output: data.results[2].output
        }
      };
  
      setResults(formattedResults);
  
      // Automatically evaluate outputs
      const autoScores = {
        A: autoEvaluateOutput(formattedResults.modelA.output, useCase),
        B: autoEvaluateOutput(formattedResults.modelB.output, useCase),
        C: autoEvaluateOutput(formattedResults.modelC.output, useCase)
      };
  
      setEvaluation((prev) => ({
        ...prev,
        scores: autoScores
      }));
    } catch (error) {
      console.error("Error generating results:", error);
      alert("Failed to generate results. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };
  
  
  // Get criteria for current use case
  const getCriteria = () => {
    return useCase === "code" 
      ? [
          { key: "correctness", label: "Correctness" },
          { key: "efficiency", label: "Efficiency" },
          { key: "readability", label: "Readability" },
          { key: "robustness", label: "Robustness" },
          { key: "documentation", label: "Documentation" }
        ]
      : [
          { key: "relevance", label: "Relevance" },
          { key: "quality", label: "Quality" },
          { key: "creativity", label: "Creativity" },
          { key: "consistency", label: "Consistency" },
          { key: "composition", label: "Composition" }
        ];
  };
  
  // Update score for a model
  const updateScore = (model, criterion, score) => {
    setEvaluation(prev => ({
      ...prev,
      scores: {
        ...prev.scores,
        [model]: {
          ...prev.scores[model],
          [criterion]: score
        }
      }
    }));
  };
  
  // Calculate total score for a model
  const getTotalScore = (model) => {
    const scores = evaluation.scores[model];
    if (!scores || Object.keys(scores).length === 0) return 0;
    
    const sum = Object.values(scores).reduce((total, score) => total + score, 0);
    return sum;
  };

  // Handle evaluation submission
  const handleEvaluationSubmit = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/submit-evaluation`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          useCase,
          prompt,
          models: {
            modelA: selectedModels[useCase].modelA,
            modelB: selectedModels[useCase].modelB,
            modelC: selectedModels[useCase].modelC
          },
          scores: evaluation.scores,
          feedback: evaluation.feedback
        })
      });
      
      if (!response.ok) {
        throw new Error("Failed to submit evaluation");
      }
      
      alert("Evaluation submitted successfully!");
    } catch (error) {
      console.error("Error submitting evaluation:", error);
      alert("Failed to submit evaluation. Please try again.");
    }
  };
  
  // Render model result card
  const renderModelCard = (modelKey, modelData) => {
    return (
      <div className="model-card">
        <div className="model-header">
          <h3 className="model-title">{modelData.name}</h3>
          <span className="model-score">
            Score: {getTotalScore(modelKey)}/50
          </span>
        </div>
        
        {/* Display output based on use case */}
        {useCase === "code" ? (
          <div className="code-output">
            <pre className="code-block">
              <code>{modelData.output}</code>
            </pre>
          </div>
        ) : (
          <div className="image-output">
            <img 
              src={modelData.output} 
              alt={`${modelData.name} generated image`} 
              className="generated-image" 
            />
          </div>
        )}
        
        {/* Scoring section */}
        <div className="rating-section">
          <h4 className="rating-title">Rate this output:</h4>
          <div className="criteria-list">
            {getCriteria().map(criterion => (
              <div key={criterion.key} className="criterion-item">
                <span className="criterion-label">{criterion.label}:</span>
                <div className="score-buttons">
                  {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(score => (
                    <button
                      key={score}
                      onClick={() => updateScore(modelKey, criterion.key, score)}
                      className={`score-button ${
                        evaluation.scores[modelKey][criterion.key] === score
                          ? "score-button-active" 
                          : ""
                      }`}
                    >
                      {score}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };
  
  return (
    <div className="app-container">
      <div className="app-content">
        <div className="app-header">
          <h1 className="app-title">LLM Comparison App</h1>
          <p className="app-description">Compare outputs from different LLM providers for code and image generation</p>
          
          <form onSubmit={handleSubmit} className="app-form">
            <div className="form-group">
              <label className="form-label">Select Use Case:</label>
              <div className="button-group">
                <button
                  type="button"
                  onClick={() => setUseCase("code")}
                  className={`use-case-button ${useCase === "code" ? "use-case-button-active" : ""}`}
                >
                  Code Generation
                </button>
                <button
                  type="button"
                  onClick={() => setUseCase("image")}
                  className={`use-case-button ${useCase === "image" ? "use-case-button-active" : ""}`}
                >
                  Image Generation
                </button>
              </div>
            </div>
            
            <div className="form-group">
              <label className="form-label">Select Models to Compare:</label>
              <div className="models-grid">
                {/* Model A Selection */}
                <div className="model-select-box">
                  <h3 className="model-select-title">Model A:</h3>
                  <select 
                    value={selectedModels[useCase].modelA}
                    onChange={(e) => handleModelChange("modelA", e.target.value)}
                    className="model-select"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
                
                {/* Model B Selection */}
                <div className="model-select-box">
                  <h3 className="model-select-title">Model B:</h3>
                  <select 
                    value={selectedModels[useCase].modelB}
                    onChange={(e) => handleModelChange("modelB", e.target.value)}
                    className="model-select"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
                
                {/* Model C Selection */}
                <div className="model-select-box">
                  <h3 className="model-select-title">Model C:</h3>
                  <select 
                    value={selectedModels[useCase].modelC}
                    onChange={(e) => handleModelChange("modelC", e.target.value)}
                    className="model-select"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
            
            <div className="form-group">
              <div className="prompt-header">
                <label className="form-label">
                  Enter your prompt for {useCase === "code" ? "code" : "image"} generation:
                </label>
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
                placeholder={useCase === "code" 
                  ? "E.g., Write a function to find the longest palindrome substring in a string" 
                  : "E.g., A futuristic cityscape with flying cars and tall glass buildings at sunset"
                }
                required
              />
            </div>
            
            {showCriteria && (
              <div className="criteria-box">
                <h3 className="criteria-title">Evaluation Criteria for {useCase === "code" ? "Code" : "Image"} Generation:</h3>
                {useCase === "code" ? (
                  <ul className="criteria-list-detailed">
                    <li><strong>Correctness</strong> - Does the code solve the problem correctly?</li>
                    <li><strong>Efficiency</strong> - Is the algorithm optimal in terms of time/space complexity?</li>
                    <li><strong>Readability</strong> - Is the code easy to understand and well-structured?</li>
                    <li><strong>Robustness</strong> - Does the code handle edge cases appropriately?</li>
                    <li><strong>Documentation</strong> - Are there helpful comments explaining the logic?</li>
                  </ul>
                ) : (
                  <ul className="criteria-list-detailed">
                    <li><strong>Relevance</strong> - How well does the image match the prompt?</li>
                    <li><strong>Quality</strong> - Is the image clear, detailed, and visually appealing?</li>
                    <li><strong>Creativity</strong> - Does the image show originality and imagination?</li>
                    <li><strong>Consistency</strong> - Are the elements in the image coherent and properly integrated?</li>
                    <li><strong>Composition</strong> - Is the layout and framing of the image well-balanced?</li>
                  </ul>
                )}
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
              {/* Model A Result */}
              {renderModelCard("A", results.modelA)}
              
              {/* Model B Result */}
              {renderModelCard("B", results.modelB)}
              
              {/* Model C Result */}
              {renderModelCard("C", results.modelC)}
            </div>
            
            {/* Additional Feedback */}
            <div className="feedback-section">
              <h3 className="feedback-title">Additional Comments (Optional)</h3>
              <textarea
                value={evaluation.feedback}
                onChange={(e) => setEvaluation({...evaluation, feedback: e.target.value})}
                className="feedback-input"
                rows="3"
                placeholder="Share any additional observations about the comparison..."
              />
              <div className="evaluation-footer">
                <div className="evaluation-summary">
                  {getTotalScore("A") > 0 && getTotalScore("B") > 0 && getTotalScore("C") > 0 ? (
                    <span className="evaluation-result">
                      {(() => {
                        const scores = [
                          { model: "A", score: getTotalScore("A"), name: results.modelA.name },
                          { model: "B", score: getTotalScore("B"), name: results.modelB.name },
                          { model: "C", score: getTotalScore("C"), name: results.modelC.name }
                        ].sort((a, b) => b.score - a.score);
                        
                        if (scores[0].score === scores[1].score && scores[1].score === scores[2].score) {
                          return "All models scored equally";
                        } else if (scores[0].score === scores[1].score) {
                          return `${scores[0].name} and ${scores[1].name} tied for highest score`;
                        } else {
                          return `${scores[0].name} scored highest with ${scores[0].score} points`;
                        }
                      })()}
                    </span>
                  ) : (
                    <span>Please rate all models to see comparison results</span>
                  )}
                </div>
                <button
                  onClick={handleEvaluationSubmit}
                  className={`evaluation-submit-button ${
                    Object.keys(evaluation.scores.A).length === 0 || 
                    Object.keys(evaluation.scores.B).length === 0 ||
                    Object.keys(evaluation.scores.C).length === 0
                      ? "evaluation-submit-button-disabled"
                      : ""
                  }`}
                  disabled={
                    Object.keys(evaluation.scores.A).length === 0 || 
                    Object.keys(evaluation.scores.B).length === 0 ||
                    Object.keys(evaluation.scores.C).length === 0
                  }
                >
                  Submit Evaluation
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}