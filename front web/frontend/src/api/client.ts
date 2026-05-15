import axios from 'axios';

const client = axios.create({
    baseURL: 'http://localhost:3000/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor to add auth token from localStorage if exists
client.interceptors.request.use((config) => {
    const userData = localStorage.getItem('smartlogix_user');
    if (userData) {
        try {
            const { token } = JSON.parse(userData);
            if (token) {
                // El token ya viene con "Bearer " desde Login.tsx
                config.headers.Authorization = token;
            } else {
                console.warn('[API Client] No token found in user data');
            }
        } catch (e) {
            console.error('[API Client] Error parsing user data', e);
        }
    } else {
        console.warn('[API Client] No user data in localStorage');
    }
    return config;
});

export const authClient = client;
export const pedidosClient = client;
export default client;
