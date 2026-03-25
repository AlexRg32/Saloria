import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
});

const removeContentTypeHeader = (config: InternalAxiosRequestConfig) => {
  if (!config.headers) {
    return;
  }

  const headers = config.headers as any;
  if (typeof headers.set === 'function') {
    headers.set('Content-Type', undefined);
    return;
  }

  delete headers['Content-Type'];
};

const ensureJsonContentType = (config: InternalAxiosRequestConfig) => {
  if (!config.headers) {
    return;
  }

  const headers = config.headers as any;
  if (typeof headers.set === 'function') {
    if (!headers.get('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }
    return;
  }

  if (!headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
};

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (config.data instanceof FormData) {
      removeContentTypeHeader(config);
    } else {
      ensureJsonContentType(config);
    }

    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      window.dispatchEvent(new Event('auth-unauthorized'));
    }
    return Promise.reject(error);
  }
);
