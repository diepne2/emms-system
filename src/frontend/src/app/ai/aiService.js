import axios from "axios";

const API = 'https://emms-system-production-4239.up.railway.app/api';

export const analyzeAI = (data) => {
  return axios.post(`${API}/analysis`, data);
};

export const quickAnalyze = (question) => {
  return axios.get(`${API}/analysis/quick`, {
    params: { question },
  });
};

export const monthlyAnalyze = (question, month, year) => {
  return axios.get(`${API}/analysis/month`, {
    params: { question, month, year },
  });
};

export const yearlyAnalyze = (question, year) => {
  return axios.get(`${API}/analysis/year`, {
    params: { question, year },
  });
};