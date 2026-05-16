import { Button } from '@aamini/ui/components/button'
import { Home } from 'lucide-react'

export function HomeButton({ className }: { className?: string }) {
	return (
		<Button className={className} variant="outline" size="icon" asChild={true}>
			<a href="/">
				<Home />
			</a>
		</Button>
	)
}
