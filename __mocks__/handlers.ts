import suggestions from './data/suggestions.json' with { type: 'json' }
import { http, HttpResponse } from '@aamini/config/msw'

export default [
	http.get('/api/suggestions', () => {
		return HttpResponse.json(suggestions)
	}),
]
