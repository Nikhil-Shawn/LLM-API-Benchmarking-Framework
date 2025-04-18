import referenceSolutions from "../data/referenceSolutions.json";

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

const normalize = (str) =>
  str
    .toLowerCase()
    .replace(/["'\n\t\r]/g, "")
    .replace(/[\s]+/g, " ")
    .replace(/[^\w\s]/g, "") // remove symbols like ;(){} etc.
    .trim();

const matchPromptToKey = (prompt) => {
  const cleanedPrompt = normalize(prompt);
  const keys = Object.keys(referenceSolutions);

  for (const key of keys) {
    const keyWords = key.toLowerCase().split(" ");
    const matches = keyWords.every((word) => cleanedPrompt.includes(word));
    if (matches) return key;
  }

  return null;
};

const isLooselyMatching = (output, refVariants) => {
  const normalizedOutput = normalize(output);
  return refVariants.some((ref) => {
    const normalizedRef = normalize(ref);
    // Check if at least 50% of the reference tokens appear in output
    const refTokens = new Set(normalizedRef.split(" "));
    const outputTokens = new Set(normalizedOutput.split(" "));
    const common = [...refTokens].filter((token) => outputTokens.has(token));
    const matchRatio = common.length / refTokens.size;
    return matchRatio >= 0.5;
  });
};

export const autoEvaluateOutput = (output, useCase, prompt = "") => {
  const criteria = getCriteria(useCase).map((c) => c.key);
  const scores = {};

  if (useCase === "code") {
    const matchedKey = matchPromptToKey(prompt);
    const ref = matchedKey ? referenceSolutions[matchedKey] : null;
    const variants = Array.isArray(ref) ? ref : ref ? [ref] : [];

    const matched = variants.length > 0 && isLooselyMatching(output, variants);

    scores["correctness"] = matched ? 10 : 3;
    scores["efficiency"] = /HashMap|StringBuilder/.test(output) ? 9 : 6;
    scores["readability"] = output.split("\n").length < 20 ? 8 : 6;
    scores["robustness"] = /null|try/.test(output) ? 8 : 6;
    scores["documentation"] = /\/\*\*/.test(output) ? 9 : 6;
  } else {
    criteria.forEach((key) => {
      scores[key] = Math.floor(Math.random() * 5) + 6;
    });
  }

  return scores;
};

export const getTotalScore = (scores) => {
  if (!scores) return 0;
  return Object.values(scores).reduce((a, b) => a + b, 0);
};
