import axios from 'axios';

const client = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080' });

export async function getAnalyticsSummary(tenantId, from, to) {
	const response = await client.get('/api/analytics/summary', {
		params: { tenantId, from: from.toISOString(), to: to.toISOString() },
	});
	return response.data;
}
