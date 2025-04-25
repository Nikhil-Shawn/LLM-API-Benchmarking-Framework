// File: src/utils/api.js

const API_BASE_URL = "http://localhost:8081";

export async function generateOutputs(useCase, prompt, models) {
  if (useCase === "code") {
    const results = [];

    for (const model of models) {
      if (model === "gemini") {
        const res = await fetch(`${API_BASE_URL}/api/gemini/generate`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(prompt)
        });

        if (!res.ok) throw new Error("Failed to get Gemini response");
        const output = await res.text();

        results.push({ model, output });
      } else if (model === "mistral") { 
        const res = await fetch(`${API_BASE_URL}/api/mistral/generate`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(prompt)
          });
          
          if (!res.ok) throw new Error("Failed to get MistralAI response");
          const output = await res.text();
          
          results.push({ model, output });
      } else {
        results.push({
          model,
          output: `Mocked output from ${model} for prompt: ${prompt}`
        });
      }
    }

    return { results };
  }

  if (useCase === "image") {
    const results = [];

    for (const model of models) {
      if (model === "gemini") {
        const res = await fetch(`${API_BASE_URL}/api/gemini/image`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(prompt)
        });

        if (!res.ok) throw new Error("Failed to get Gemini image");
        const output = await res.text();

        results.push({ model, output });
      } else {
        results.push({
          model,
          output: `Mocked image response from ${model} for prompt: ${prompt}`
        });
      }
    }

    return { results };
  }
}

export async function submitEvaluation(data) {
  const response = await fetch(`${API_BASE_URL}/api/submit-evaluation`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });

  if (!response.ok) throw new Error("Failed to submit evaluation");
  return response.json();
}
