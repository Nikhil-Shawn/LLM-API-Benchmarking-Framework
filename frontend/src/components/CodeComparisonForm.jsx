import React, { useState } from "react";
import { generateOutputs } from "../services/api";
import { autoEvaluateOutput, getCriteria, getTotalScore } from "../utils/evaluation";
import ComparisonFormUI from "./ComparisonFormUI";

const MODEL_OPTIONS = [
  { id: "starcoder", name: "StarCoder (HuggingFace)" },
  { id: "mistral",   name: "Mistral AI" },
  { id: "gemini",    name: "Gemini Pro" }
];

export default function CodeComparisonForm() {
  const useCase = "code";
  const [prompt, setPrompt] = useState("");
  const [selectedModels, setSelectedModels] = useState({
    modelA: "starcoder",
    modelB: "mistral",
    modelC: "gemini"
  });
  const [isLoading, setIsLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [scores, setScores] = useState({ A: 0, B: 0, C: 0 });
  const [showCriteria, setShowCriteria] = useState(false);

  const onModelChange = (slot, id) => {
    setSelectedModels(prev => ({ ...prev, [slot]: id }));
  };

  const getModelName = slot => {
    const opt = MODEL_OPTIONS.find(m => m.id === selectedModels[slot]);
    return opt ? opt.name : selectedModels[slot];
  };

  const handleSubmit = async e => {
    e.preventDefault();
    if (!prompt.trim()) return;
    setIsLoading(true);
    try {
      const ids = ["modelA","modelB","modelC"].map(k => selectedModels[k]);
      const { results: raw } = await generateOutputs(useCase, prompt, ids);

      const formatted = {
        modelA: { name: getModelName("modelA"), output: raw[0]?.output || "" },
        modelB: { name: getModelName("modelB"), output: raw[1]?.output || "" },
        modelC: { name: getModelName("modelC"), output: raw[2]?.output || "" }
      };
      setResults(formatted);

      setScores({
        A: autoEvaluateOutput(formatted.modelA.output, useCase, prompt),
        B: autoEvaluateOutput(formatted.modelB.output, useCase, prompt),
        C: autoEvaluateOutput(formatted.modelC.output, useCase, prompt)
      });
    } catch (err) {
      console.error("Error generating code outputs:", err);
      alert("Failed to generate code outputs: " + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ComparisonFormUI
      useCase={useCase}                      // pass 'code' to UI
      prompt={prompt}
      setPrompt={setPrompt}
      isLoading={isLoading}
      onSubmit={handleSubmit}
      modelOptions={MODEL_OPTIONS}
      selectedModels={selectedModels}
      onModelChange={onModelChange}
      showCriteria={showCriteria}
      toggleCriteria={() => setShowCriteria(c => !c)}
      criteria={getCriteria(useCase)}
      results={results}
      scores={scores}
      getTotalScore={getTotalScore}
    />
  );
}
