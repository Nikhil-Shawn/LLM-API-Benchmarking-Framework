import React from "react";
import { Routes, Route, Navigate, NavLink } from "react-router-dom";
import CodeComparisonPage from "../pages/CodeComparisonPage";
import ImageComparisonPage from "../pages/ImageComparisonPage";
import '../App.css';

export default function LLMComparisonApp() {
  return (
    <div className="app-container">
      <nav className="app-nav">
        <NavLink
          to="/code"
          className={({ isActive }) =>
            isActive ? "nav-btn nav-btn--active" : "nav-btn"
          }
        >
          Code
        </NavLink>
        <NavLink
          to="/image"
          className={({ isActive }) =>
            isActive ? "nav-btn nav-btn--active" : "nav-btn"
          }
        >
          Image
        </NavLink>
      </nav>

      <Routes>
        <Route path="/code" element={<CodeComparisonPage />} />
        <Route path="/image" element={<ImageComparisonPage />} />
        <Route path="*" element={<Navigate to="/code" replace />} />
      </Routes>
    </div>
  );
}
