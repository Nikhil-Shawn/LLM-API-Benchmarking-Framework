const API_BASE_URL = "http://localhost:8081";

export async function generateOutputs(useCase, prompt, models) {
  // For code generation
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
      } else {
        // You could also mock or call OpenAI, HuggingFace etc. here
        results.push({
          model,
          output: `Mocked output from ${model} for prompt: ${prompt}`
        });
      }
    }

    return { results };
  }

  // For image generation, use the unified backend
  const endpoint = `${API_BASE_URL}/api/generate-image`;

  const response = await fetch(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ prompt, models })
  });

  if (!response.ok) throw new Error("Failed to generate image results");
  return response.json();
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
