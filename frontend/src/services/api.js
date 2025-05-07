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
        case "codeLlama": {
          const res = await fetch(`${API_BASE_URL}/api/code-llama/generate`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ prompt }),
          });
          if (!res.ok) throw new Error("Failed to get Code Llama response");
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

  // image section remains unchanged...
}
