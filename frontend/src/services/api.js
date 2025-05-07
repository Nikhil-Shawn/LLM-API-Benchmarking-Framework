// src/services/api.js

import { getCriteria } from "../utils/evaluation";

const API_BASE_URL = "http://localhost:8081";

function buildEvalPrompt(userPrompt, images, criteria) {
  const critList = criteria.map(c => `- ${c.label}`).join("\n");
  return [
    `You must respond ONLY with valid JSON and nothing else.`,
    `I asked for: "${userPrompt}"`,
    `Evaluation criteria:\n${critList}`,
    `Here are the three candidate images (A, B, C):`,
    `A: ${images[0]}`,
    `B: ${images[1]}`,
    `C: ${images[2]}`,
    ``,
    `Please rate each image on a scale from 0 (worst) to 10 (best) for each criterion,`,
    `then give me a total score for each (A, B, C) and tell me which one wins.`,
    `Respond in JSON like:`,
    `{"scores":{"A":32,"B":28,"C":40},"winner":"C"}`
  ].join("\n");
}

export async function generateOutputs(useCase, prompt, models) {
  if (useCase === "code") {
    const results = [];
    for (const model of models) {
      switch (model) {
        case "gemini": {
          const res = await fetch(`${API_BASE_URL}/api/gemini/generate`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ prompt }),
          });
          if (!res.ok) throw new Error("Failed to get Gemini response");
          results.push({ model, output: await res.text() });
          break;
        }
        case "starcoder": {
          const res = await fetch(`${API_BASE_URL}/api/starcoder/generate`, {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: prompt,
          });
          if (!res.ok) throw new Error("Failed to get StarCoder response");
          results.push({ model, output: await res.text() });
          break;
        }
        case "mistral": {
          const res = await fetch(`${API_BASE_URL}/api/mistral/generate`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ prompt }),
          });
          if (!res.ok) throw new Error("Failed to get MistralAI response");
          results.push({ model, output: await res.text() });
          break;
        }
        default:
          results.push({
            model,
            output: `Mocked output from ${model} for prompt: ${prompt}`,
          });
      }
    }
    return { results };
  }

  if (useCase === "image") {
    // 1) generate the three images
    const results = [];
    for (const model of models) {
      switch (model) {
        case "gemini": {
          const res = await fetch(`${API_BASE_URL}/api/gemini/image`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ prompt }),
          });
          if (!res.ok) throw new Error("Failed to get Gemini image");
          results.push({ model, output: await res.text() });
          break;
        }
        case "stableDiffusion": {
          const res = await fetch(`${API_BASE_URL}/api/sdxl/generate`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ prompt }),
          });
          if (!res.ok) throw new Error("Failed to get SDXL image");
          const { results: sdxlResults } = await res.json();
          sdxlResults.forEach(({ model: m, output }) =>
            results.push({ model: m, output })
          );
          break;
        }
        case "aihorde": {
          const res = await fetch(`${API_BASE_URL}/api/aihorde/image`, {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: prompt,
          });
          if (!res.ok) throw new Error("Failed to get AI Horde image");
          results.push({ model, output: await res.text() });
          break;
        }
        default:
          results.push({ model, output: "" });
      }
    }

    // 2) collect the three data-URIs (or URLs) into an array
    const images = results.map(r => r.output);

    // 3) call your scoring API with those three raw strings
    const scoreRes = await fetch(`${API_BASE_URL}/api/image/score`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ prompt, images }),
    });
    if (!scoreRes.ok) throw new Error("Image scoring failed");

    const { scores, winner } = await scoreRes.json();

    // 4) return both the raw images *and* the scores + winner
    return { results, scores, winner };
  }

  throw new Error(`Unknown useCase: ${useCase}`);
}
