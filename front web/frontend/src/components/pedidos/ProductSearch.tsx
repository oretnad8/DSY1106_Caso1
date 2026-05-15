import { useState, useEffect } from 'react';
import { Search, Plus, Package } from 'lucide-react';
import client from '../../api/client';
import type { Producto } from '../../types';

interface ProductSearchProps {
    onSelect: (producto: Producto) => void;
}

export const ProductSearch = ({ onSelect }: ProductSearchProps) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<Producto[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const timer = setTimeout(async () => {
            if (query.length < 2) {
                setResults([]);
                return;
            }

            setLoading(true);
            try {
                // El BFF ya maneja el forwarding del token y el fallback de SKU
                const response = await client.get(`/pedidos/productos?query=${query}`);
                setResults(response.data);
            } catch (error) {
                console.error('Error searching products:', error);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [query]);

    return (
        <div className="relative">
            <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Buscar producto por SKU o nombre..."
                    className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>

            {results.length > 0 && (
                <div className="absolute z-10 w-full mt-2 bg-white border border-gray-200 rounded-lg shadow-xl max-h-64 overflow-auto">
                    {results.map((p) => (
                        <button
                            key={p.sku}
                            onClick={() => {
                                onSelect(p);
                                setQuery('');
                                setResults([]);
                            }}
                            className="w-full flex items-center justify-between px-4 py-3 hover:bg-blue-50 transition-colors text-left border-b border-gray-100 last:border-0"
                        >
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-gray-100 rounded text-gray-500">
                                    <Package size={16} />
                                </div>
                                <div>
                                    <div className="font-medium text-sm text-gray-900">{p.descripcion}</div>
                                    <div className="text-xs text-gray-500">SKU: {p.sku} • Stock: {p.stock}</div>
                                </div>
                            </div>
                            <Plus size={18} className="text-blue-600" />
                        </button>
                    ))}
                </div>
            )}

            {loading && (
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                    <div className="w-4 h-4 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
            )}
        </div>
    );
};
