export const getCriteria = (useCase) =>
    useCase === "code"
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
  
  export const autoEvaluateOutput = (output, useCase) => {
    const criteria = getCriteria(useCase).map(c => c.key);
    const scores = {};
    criteria.forEach((key) => {
      scores[key] = Math.floor(Math.random() * 5) + 6;
    });
    return scores;
  };
  
  export const getTotalScore = (scores) => {
    if (!scores) return 0;
    return Object.values(scores).reduce((a, b) => a + b, 0);
  };
  