import { getCriteria } from "../utils/evaluation";

export default function ModelCard({
  modelKey,
  modelData,
  useCase,
  evaluation,
  updateScore
}) {
  const scores = evaluation.scores[modelKey];

  return (
    <div className="model-card">
      <div className="model-header">
        <h3 className="model-title">{modelData.name}</h3>
        <span className="model-score">
          Score: {Object.values(scores).reduce((a, b) => a + b, 0)}/50
        </span>
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
        <h4 className="rating-title">Rate this output:</h4>
        <div className="criteria-list">
          {getCriteria(useCase).map((criterion) => (
            <div key={criterion.key} className="criterion-item">
              <span className="criterion-label">{criterion.label}:</span>
              <div className="score-buttons">
                {[1,2,3,4,5,6,7,8,9,10].map(score => (
                  <button
                    key={score}
                    onClick={() => updateScore(modelKey, criterion.key, score)}
                    className={`score-button ${
                      scores[criterion.key] === score ? "score-button-active" : ""
                    }`}
                  >
                    {score}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
