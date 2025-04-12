import { useState } from "react";

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
      // API endpoint based on use case
      const endpoint = useCase === "code" ? "/api/generate-code" : "/api/generate-image";
      
      // Prepare models for API request
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
        body: JSON.stringify({
          prompt,
          models
        })
      });
      
      if (!response.ok) {
        throw new Error("Failed to generate results");
      }
      
      const data = await response.json();
      
      // Format results for display
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
      const response = await fetch("/api/submit-evaluation", {
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
      <div className="p-4 border-2 border-gray-200 rounded-lg">
        <div className="flex justify-between items-center mb-3 pb-2 border-b">
          <h3 className="font-bold text-lg">{modelData.name}</h3>
          <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
            Score: {getTotalScore(modelKey)}/50
          </span>
        </div>
        
        {/* Display output based on use case */}
        {useCase === "code" ? (
          <div className="bg-gray-800 text-gray-100 p-4 rounded-md overflow-auto max-h-96 mb-4">
            <pre className="text-sm font-mono">
              <code>{modelData.output}</code>
            </pre>
          </div>
        ) : (
          <div className="flex justify-center mb-4">
            <img 
              src={modelData.output} 
              alt={`${modelData.name} generated image`} 
              className="max-h-64 object-contain rounded-md border border-gray-300" 
            />
          </div>
        )}
        
        {/* Scoring section */}
        <div className="mt-4">
          <h4 className="font-medium mb-2">Rate this output:</h4>
          <div className="space-y-3">
            {getCriteria().map(criterion => (
              <div key={criterion.key} className="flex items-center justify-between">
                <span className="text-sm">{criterion.label}:</span>
                <div className="flex space-x-1">
                  {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(score => (
                    <button
                      key={score}
                      onClick={() => updateScore(modelKey, criterion.key, score)}
                      className={`w-6 h-6 flex items-center justify-center text-xs rounded ${
                        evaluation.scores[modelKey][criterion.key] === score
                          ? "bg-blue-600 text-white" 
                          : "bg-gray-200 hover:bg-gray-300"
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
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-6xl mx-auto">
        <div className="bg-white p-6 rounded-lg shadow-lg mb-6">
          <h1 className="text-3xl font-bold mb-2 text-center">LLM Comparison App</h1>
          <p className="text-gray-600 text-center mb-6">Compare outputs from different LLM providers for code and image generation</p>
          
          <form onSubmit={handleSubmit} className="mb-6">
            <div className="mb-4">
              <label className="block text-gray-700 mb-2 font-medium">Select Use Case:</label>
              <div className="flex space-x-4">
                <button
                  type="button"
                  onClick={() => setUseCase("code")}
                  className={`px-4 py-2 rounded-md ${useCase === "code" 
                    ? "bg-blue-600 text-white" 
                    : "bg-gray-200 text-gray-800 hover:bg-gray-300"}`}
                >
                  Code Generation
                </button>
                <button
                  type="button"
                  onClick={() => setUseCase("image")}
                  className={`px-4 py-2 rounded-md ${useCase === "image" 
                    ? "bg-blue-600 text-white" 
                    : "bg-gray-200 text-gray-800 hover:bg-gray-300"}`}
                >
                  Image Generation
                </button>
              </div>
            </div>
            
            <div className="mb-6">
              <label className="block text-gray-700 mb-2 font-medium">Select Models to Compare:</label>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Model A Selection */}
                <div className="border border-gray-300 rounded-md p-4">
                  <h3 className="font-medium mb-2">Model A:</h3>
                  <select 
                    value={selectedModels[useCase].modelA}
                    onChange={(e) => handleModelChange("modelA", e.target.value)}
                    className="w-full p-2 border border-gray-300 rounded-md"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
                
                {/* Model B Selection */}
                <div className="border border-gray-300 rounded-md p-4">
                  <h3 className="font-medium mb-2">Model B:</h3>
                  <select 
                    value={selectedModels[useCase].modelB}
                    onChange={(e) => handleModelChange("modelB", e.target.value)}
                    className="w-full p-2 border border-gray-300 rounded-md"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
                
                {/* Model C Selection */}
                <div className="border border-gray-300 rounded-md p-4">
                  <h3 className="font-medium mb-2">Model C:</h3>
                  <select 
                    value={selectedModels[useCase].modelC}
                    onChange={(e) => handleModelChange("modelC", e.target.value)}
                    className="w-full p-2 border border-gray-300 rounded-md"
                  >
                    {currentModels.map(model => (
                      <option key={model.id} value={model.id}>{model.name}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
            
            <div className="mb-4">
              <div className="flex justify-between items-center mb-2">
                <label className="block text-gray-700 font-medium">
                  Enter your prompt for {useCase === "code" ? "code" : "image"} generation:
                </label>
                <button 
                  type="button"
                  onClick={() => setShowCriteria(!showCriteria)}
                  className="text-blue-600 hover:text-blue-800 text-sm"
                >
                  {showCriteria ? "Hide evaluation criteria" : "Show evaluation criteria"}
                </button>
              </div>
              <textarea
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                className="w-full border border-gray-300 p-3 rounded"
                rows="4"
                placeholder={useCase === "code" 
                  ? "E.g., Write a function to find the longest palindrome substring in a string" 
                  : "E.g., A futuristic cityscape with flying cars and tall glass buildings at sunset"
                }
                required
              />
            </div>
            
            {showCriteria && (
              <div className="mb-4 p-4 bg-blue-50 rounded-md border border-blue-200">
                <h3 className="font-semibold text-blue-800 mb-2">Evaluation Criteria for {useCase === "code" ? "Code" : "Image"} Generation:</h3>
                {useCase === "code" ? (
                  <ul className="list-disc pl-5 space-y-1 text-sm text-blue-900">
                    <li><strong>Correctness</strong> - Does the code solve the problem correctly?</li>
                    <li><strong>Efficiency</strong> - Is the algorithm optimal in terms of time/space complexity?</li>
                    <li><strong>Readability</strong> - Is the code easy to understand and well-structured?</li>
                    <li><strong>Robustness</strong> - Does the code handle edge cases appropriately?</li>
                    <li><strong>Documentation</strong> - Are there helpful comments explaining the logic?</li>
                  </ul>
                ) : (
                  <ul className="list-disc pl-5 space-y-1 text-sm text-blue-900">
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
              className="bg-blue-600 text-white py-2 px-6 rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
            >
              {isLoading ? "Processing..." : "Generate and Compare"}
            </button>
          </form>
        </div>
        
        {results && (
          <div className="bg-white p-6 rounded-lg shadow-lg mb-6">
            <h2 className="text-xl font-bold mb-4">Comparison Results:</h2>
            
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Model A Result */}
              {renderModelCard("A", results.modelA)}
              
              {/* Model B Result */}
              {renderModelCard("B", results.modelB)}
              
              {/* Model C Result */}
              {renderModelCard("C", results.modelC)}
            </div>
            
            {/* Additional Feedback */}
            <div className="mt-6 p-5 bg-gray-50 rounded-lg border border-gray-200">
              <h3 className="font-medium mb-3">Additional Comments (Optional)</h3>
              <textarea
                value={evaluation.feedback}
                onChange={(e) => setEvaluation({...evaluation, feedback: e.target.value})}
                className="w-full border border-gray-300 p-3 rounded-md mb-4"
                rows="3"
                placeholder="Share any additional observations about the comparison..."
              />
              <div className="flex justify-between items-center">
                <div className="text-gray-600">
                  {getTotalScore("A") > 0 && getTotalScore("B") > 0 && getTotalScore("C") > 0 ? (
                    <span className="font-medium">
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
                  className="bg-green-600 text-white py-2 px-6 rounded-md hover:bg-green-700 disabled:bg-green-300"
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