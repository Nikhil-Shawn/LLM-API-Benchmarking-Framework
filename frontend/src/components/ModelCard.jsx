import { getCriteria } from "../utils/evaluation";

export default function ModelCard({ modelKey, modelData, scores = {}, useCase }) {
  const totalScore = typeof scores === "number"
    ? scores
    : Object.values(scores).reduce((a, b) => a + b, 0);

  return (
    <div className="model-card">
      <div className="model-header">
        <h3 className="model-title" style={{ fontSize: "1rem", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
          {modelData.name}
        </h3>
        <span className="model-score">Score: {totalScore}</span>
      </div>

      {useCase === "code" ? (
        <div className="code-output">
          <pre className="code-block">
            <code>{modelData.output}</code>
          </pre>
        </div>
      ) : (
        <div className="image-output">
          <img src={modelData.output} alt={`${modelData.name} output`} className="generated-image" />
        </div>
      )}

      <div className="rating-section">
        <h4 className="rating-title">Scores by Criteria:</h4>
        <ul className="criteria-list">
  {getCriteria(useCase).map((criterion) => (
    <li key={criterion.key} style={{ color: "navy", fontWeight: "500" }}>
      {criterion.label}
    </li>
  ))}
</ul>
      </div>
    </div>
  );
}
