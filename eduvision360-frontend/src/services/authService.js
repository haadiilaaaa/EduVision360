import axios from "axios";

const API = "http://localhost:8080/api/auth";

export const registerUser = (data) =>
  axios.post(`${API}/register`, data);

export const verifyOtp = (data) =>
  axios.post(`${API}/verify-otp`, data);

export const resendOtp = (email) =>
  axios.post(`${API}/resend-otp`, null, {
    params: { email }
  });

export const login = (data) =>
  axios.post(`${API}/login`, data);
