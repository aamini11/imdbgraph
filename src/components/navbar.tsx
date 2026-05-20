import { Button } from './ui/button'
import { Home, LogIn } from 'lucide-react'
import type { ReactNode } from 'react'

interface NavbarProps {
	center?: ReactNode
}

export function Navbar({ center }: NavbarProps) {
	return (
		<nav className="grid grid-cols-[1fr_minmax(0,28rem)_1fr] items-center gap-2 border-b px-4 py-3 md:px-6">
			<Button
				className="justify-self-start"
				variant="outline"
				size="icon"
				asChild={true}
			>
				<a href="/">
					<Home />
				</a>
			</Button>
			{center && <div className="col-start-2 w-full">{center}</div>}
			<Button
				variant="default"
				size="sm"
				className="col-start-3 gap-2 justify-self-end"
			>
				<LogIn className="h-4 w-4" />
				Log In
			</Button>
		</nav>
	)
}
