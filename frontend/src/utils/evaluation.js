import referenceSolutions from "../data/referenceSolutions.json";

export const getCriteria = (useCase) =>
  useCase === "code"
    ? [{ key: "score", label: "Score" }]
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
    .replace(/[^\w\s]/g, "")
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

async function runPythonEval(code, testCases) {
  if (!code || code.length < 10) return 0; // Too short or missing

  try {
    const res = await fetch("http://localhost:8081/api/eval/python", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, testCases })
    });

    const output = await res.text();

    if (/Traceback|SyntaxError|NameError|IndentationError/i.test(output)) {
      return 0;
    }

    const total = (testCases.match(/assert/g) || []).length;
    const failed = (output.match(/AssertionError/g) || []).length;
    const passed = total - failed;

    return total > 0 ? Math.round((passed / total) * 10) : 0;
  } catch (e) {
    console.error("Python eval failed:", e);
    return 0;
  }
}

export const autoEvaluateOutput = async (output, useCase, prompt = "") => {
  const scores = {};

  if (useCase === "code") {
    const matchedKey = matchPromptToKey(prompt);
    const ref = matchedKey ? referenceSolutions[matchedKey] : null;
    const testCases = ref?.testCases || "";

    scores["score"] = testCases
      ? await runPythonEval(output, testCases)
      : 0;
  } else {
    getCriteria(useCase).forEach((key) => {
      scores[key.key] = Math.floor(Math.random() * 5) + 6;
    });
  }

  return scores;
};

export const getTotalScore = (scores) => {
  if (!scores) return 0;
  if (typeof scores === "number") return scores;
  return Object.values(scores).reduce((a, b) => a + b, 0);
};
