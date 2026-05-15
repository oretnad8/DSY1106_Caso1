const express = require('express');
const axios = require('axios');
const router = express.Router();

const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:8081';

router.post('/login', async (req, res) => {
    try {
        const response = await axios.post(`${AUTH_SERVICE_URL}/login`, req.body);

        // Forward the token/user data
        // If the auth service returns a JWT, we can set it as a cookie here or return it
        // For requirement: "Guardar el JWT en una cookie HttpOnly o localStorage"
        // Let's return it to frontend for localStorage for simplicity as per common SPA pattern, 
        // or set cookie if the auth service response structure supports it.
        // Assuming auth service returns { token: "..." }

        res.json(response.data);
    } catch (error) {
        console.error('Auth Service Error:', error.message);
        if (error.response) {
            res.status(error.response.status).json(error.response.data);
        } else {
            res.status(500).json({ message: 'Error communicating with Auth Service' });
        }
    }
});

module.exports = router;
