const express = require('express');
const axios = require('axios');
const router = express.Router();

const PEDIDOS_SERVICE_URL = process.env.PEDIDOS_SERVICE_URL || 'http://localhost:8086/api/pedidos';

// Helper for proxying requests
const proxyRequest = async (method, path, data, headers, res) => {
    try {
        const config = {
            method,
            url: `${PEDIDOS_SERVICE_URL}${path}`,
            data,
            headers: {
                'Authorization': headers.authorization,
                'Content-Type': 'application/json'
            },
            timeout: 10000 // 10s para creación de pedidos
        };

        console.log(`[BFF] Proxying ${method.toUpperCase()} to ${config.url}`);
        console.log(`[BFF] Auth Header: ${headers.authorization ? headers.authorization.substring(0, 15) + '...' : 'NONE'}`);
        if (data) console.log(`[BFF] Payload:`, JSON.stringify(data, null, 2));

        const response = await axios(config);
        res.json(response.data);
    } catch (error) {
        console.error(`[BFF] Pedidos Service Error [${method} ${path}]: ${error.message}`);
        if (error.code) console.error(`[BFF] Error Code: ${error.code}`);

        if (error.response) {
            console.error(`[BFF] Microservice Response Error Body:`, JSON.stringify(error.response.data, null, 2));
            res.status(error.response.status).json(error.response.data);
        } else {
            res.status(500).json({ message: 'Error communicating with Pedidos Service' });
        }
    }
};

// POST /api/pedidos - Crear NV
router.post('/', (req, res) => {
    proxyRequest('post', '', req.body, req.headers, res);
});

// GET /api/pedidos/pendientes - Listado inicial (Monitor)
router.get('/pendientes', (req, res) => {
    proxyRequest('get', '/pendientes', null, req.headers, res);
});

// GET /api/pedidos/todos - Histórico completo (Reportes)
router.get('/todos', (req, res) => {
    proxyRequest('get', '/todos', null, req.headers, res);
});

// GET /api/pedidos/productos - Buscador de productos
router.get('/productos', async (req, res) => {
    // URL Base: http://localhost:8083/api/productos
    const PRODUCT_SERVICE_URL = process.env.PRODUCT_SERVICE_URL || 'http://localhost:8083/api/productos';
    const query = req.query.q || req.query.query || '';
    const authHeader = req.headers.authorization;

    const config = {
        headers: {
            'Authorization': authHeader,
            'Content-Type': 'application/json'
        },
        timeout: 5000
    };

    console.log(`[BFF] Product Search Attempt: ${query}`);

    let products = [];

    try {
        // Intento 1: Búsqueda general por texto (devuelve lista)
        // Endpoint: /api/productos/search?q={query}
        const searchUrl = `${PRODUCT_SERVICE_URL}/search?q=${encodeURIComponent(query)}`;
        console.log(`[BFF] Trying Search: ${searchUrl}`);

        const queryRes = await axios.get(searchUrl, config);
        products = queryRes.data || [];
        console.log(`[BFF] Search success: ${Array.isArray(products) ? products.length : 1} results`);
    } catch (error) {
        console.log(`[BFF] Search API failed: ${error.message}. Trying direct SKU lookup...`);
    }

    // Intento 2: Búsqueda directa por SKU (devuelve objeto único)
    // Endpoint: /api/productos/{sku}
    if ((!products || (Array.isArray(products) && products.length === 0)) && query.length > 0) {
        try {
            const skuUrl = `${PRODUCT_SERVICE_URL}/${encodeURIComponent(query)}`;
            console.log(`[BFF] Trying SKU: ${skuUrl}`);

            const skuRes = await axios.get(skuUrl, config);
            if (skuRes.data && (skuRes.data.sku || skuRes.data.id)) {
                products = [skuRes.data]; // Normalizar a lista de 1 elemento
                console.log(`[BFF] SKU direct found: ${skuRes.data.sku}`);
            }
        } catch (skuErr) {
            console.error(`[BFF] SKU direct also failed: ${skuErr.message}`);
        }
    }

    // Respuesta final normalizada
    const result = Array.isArray(products) ? products : (products ? [products] : []);
    res.json(result);
});

// GET /api/pedidos/stats - Estadísticas
router.get('/stats', (req, res) => {
    proxyRequest('get', '/stats', null, req.headers, res);
});


// PUT /api/pedidos/{id}/registrar-factura
router.put('/:id/registrar-factura', (req, res) => {
    proxyRequest('put', `/${req.params.id}/registrar-factura`, req.body, req.headers, res);
});

// POST /api/pedidos/{id}/revision-factura
router.post('/:id/revision-factura', (req, res) => {
    proxyRequest('post', `/${req.params.id}/revision-factura`, req.body, req.headers, res);
});

// --- GESTION DE VENDEDORES (Puerto 8086 /api/vendedores) ---

// GET /api/pedidos/vendedores - Listar
router.get('/vendedores/all', (req, res) => {
    proxyRequest('get', '/../vendedores', null, req.headers, res);
    // Nota: El proxyRequest base usa PEDIDOS_SERVICE_URL (http://.../api/pedidos)
    // Usamos /../vendedores para saltar a /api/vendedores
});

// POST /api/pedidos/vendedores - Crear
router.post('/vendedores', (req, res) => {
    proxyRequest('post', '/../vendedores', req.body, req.headers, res);
});

// PUT /api/pedidos/vendedores/{id} - Editar
router.put('/vendedores/:id', (req, res) => {
    proxyRequest('put', `/../vendedores/${req.params.id}`, req.body, req.headers, res);
});

// DELETE /api/pedidos/vendedores/{id} - Desactivar
router.delete('/vendedores/:id', (req, res) => {
    proxyRequest('delete', `/../vendedores/${req.params.id}`, null, req.headers, res);
});

// GET /api/pedidos/{id} - Detalle de pedido (must be LAST to avoid catching named routes)
router.get('/:id', (req, res) => {
    proxyRequest('get', `/${req.params.id}`, null, req.headers, res);
});

module.exports = router;
