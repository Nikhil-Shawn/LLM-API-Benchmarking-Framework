import React, { useState } from "react";
import { generateOutputs } from "../services/api";
import { getCriteria, getTotalScore } from "../utils/evaluation";
import ComparisonFormUI from "./ComparisonFormUI";

export default function ImageComparisonForm() {
  const useCase = "image";
  const [prompt, setPrompt] = useState("");
  const [sel, setSel]       = useState({
    modelA:"stableDiffusion",modelB:"aihorde",modelC:"gemini"
  });
  const [loading,setLoading]= useState(false);
  const [results,setResults]= useState(null);
  const [scores,setScores]  = useState({A:0,B:0,C:0});
  const [showC,setShowC]    = useState(false);

  const handleSubmit = async e=>{
    e.preventDefault();
    setLoading(true);
    try {
      const ids = ["modelA","modelB","modelC"].map(s=>sel[s]);
      const { results: raw, scores: scored } =
        await generateOutputs(useCase,prompt,ids);
      // raw: [ {model,output},… ]
      const f = {
        modelA:{ name: raw[0].model, output:raw[0].output },
        modelB:{ name: raw[1].model, output:raw[1].output },
        modelC:{ name: raw[2].model, output:raw[2].output }
      };
      setResults(f);
      setScores(scored);
    } catch {
      alert("Error");
    } finally{ setLoading(false); }
  };

  return (
    <ComparisonFormUI
      prompt={prompt} setPrompt={setPrompt}
      isLoading={loading} onSubmit={handleSubmit}
      modelOptions={[
        {id:"stableDiffusion",name:"Stable Diffusion XL"},
        {id:"aihorde",        name:"AIHorde"},
        {id:"gemini",         name:"Gemini (Google)"}
      ]}
      selectedModels={sel}
      onModelChange={(slot,id)=>setSel(s=>({...s,[slot]:id}))}
      showCriteria={showC}
      toggleCriteria={()=>setShowC(c=>!c)}
      criteria={getCriteria(useCase)}
      results={results}
      scores={scores}
      getTotalScore={getTotalScore}
    />
  );
}
