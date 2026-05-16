const express = require('express');
const axios = require('axios');
const router = express.Router();

const USUARIOS_SERVICE_URL = process.env.USUARIOS_SERVICE_URL || 'http://localhost:8082/api/usuarios';

// Helper for proxying requests
const proxyRequest = async (method, path, data, headers, res) => {
    try {
        const config = {
            method,
            url: `${USUARIOS_SERVICE_URL}${path}`,
            data,
            headers: {
                'Authorization': headers.authorization,
                'Content-Type': 'application/json'
            },
            timeout: 5000
        };

        console.log(`[BFF] Proxying User Action: ${method.toUpperCase()} to ${config.url}`);

        const response = await axios(config);
        res.json(response.data);
    } catch (error) {
        console.error(`[BFF] Usuarios Service Error [${method} ${path}]:`, error.message);
        if (error.response) {
            res.status(error.response.status).json(error.response.data);
        } else {
            res.status(500).json({ message: 'Error communicating with Usuarios Service' });
        }
    }
};

// GET /api/usuarios - Listar todos
router.get('/', (req, res) => {
    proxyRequest('get', '', null, req.headers, res);
});

// POST /api/usuarios - Crear
router.post('/', (req, res) => {
    proxyRequest('post', '', req.body, req.headers, res);
});

// PUT /api/usuarios/{id} - Actualizar
router.put('/:id', (req, res) => {
    proxyRequest('put', `/${req.params.id}`, req.body, req.headers, res);
});

// PUT /api/usuarios/{id}/toggle-activo - Toggle activo
router.put('/:id/toggle-activo', (req, res) => {
    proxyRequest('put', `/${req.params.id}/toggle-activo`, null, req.headers, res);
});

module.exports = router;
