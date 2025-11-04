import { Coffee, UtensilsCrossed } from "lucide-react";

interface SocialPlace {
  id: number;
  name: string;
  type: "restaurant" | "coffee";
  position: { top: string; left: string };
}

const socialPlaces: SocialPlace[] = [
  { id: 1, name: "The Coffee House", type: "coffee", position: { top: "25%", left: "35%" } },
  { id: 2, name: "Bistro 42", type: "restaurant", position: { top: "35%", left: "60%" } },
  { id: 3, name: "Brew & Bean", type: "coffee", position: { top: "50%", left: "25%" } },
  { id: 4, name: "La Tavola", type: "restaurant", position: { top: "45%", left: "70%" } },
  { id: 5, name: "Grind Coffee", type: "coffee", position: { top: "65%", left: "45%" } },
  { id: 6, name: "Sunset Grill", type: "restaurant", position: { top: "70%", left: "65%" } },
  { id: 7, name: "Morning Brew", type: "coffee", position: { top: "30%", left: "50%" } },
  { id: 8, name: "The Garden", type: "restaurant", position: { top: "55%", left: "55%" } },
];

export function MapScreen() {
  return (
    <div className="flex flex-col h-full bg-slate-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b shrink-0">
        <h2>Buzz In</h2>
        <button className="p-2">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
        </button>
      </div>

      {/* Map View */}
      <div className="flex-1 relative bg-gradient-to-br from-slate-100 to-slate-200">
        {/* Stylized map background */}
        <div className="absolute inset-0 opacity-20">
          <svg className="w-full h-full" viewBox="0 0 100 100" preserveAspectRatio="none">
            <pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse">
              <path d="M 10 0 L 0 0 0 10" fill="none" stroke="gray" strokeWidth="0.5"/>
            </pattern>
            <rect width="100" height="100" fill="url(#grid)" />
          </svg>
        </div>

        {/* Streets overlay */}
        <svg className="absolute inset-0 w-full h-full opacity-30" viewBox="0 0 100 100" preserveAspectRatio="none">
          <line x1="0" y1="30" x2="100" y2="30" stroke="#94a3b8" strokeWidth="0.5" />
          <line x1="0" y1="60" x2="100" y2="60" stroke="#94a3b8" strokeWidth="0.5" />
          <line x1="30" y1="0" x2="30" y2="100" stroke="#94a3b8" strokeWidth="0.5" />
          <line x1="70" y1="0" x2="70" y2="100" stroke="#94a3b8" strokeWidth="0.5" />
          <line x1="0" y1="45" x2="100" y2="45" stroke="#cbd5e1" strokeWidth="0.3" />
          <line x1="50" y1="0" x2="50" y2="100" stroke="#cbd5e1" strokeWidth="0.3" />
        </svg>

        {/* Social place markers on map */}
        {socialPlaces.map((place) => (
          <div
            key={place.id}
            className="absolute transform -translate-x-1/2 -translate-y-1/2 cursor-pointer hover:scale-110 transition-transform"
            style={{ top: place.position.top, left: place.position.left }}
          >
            <div className="relative">
              {/* Pin marker */}
              <div className="w-10 h-10 rounded-full bg-yellow-400 shadow-lg flex items-center justify-center border-2 border-white">
                {place.type === "coffee" ? (
                  <Coffee className="w-5 h-5 text-white" />
                ) : (
                  <UtensilsCrossed className="w-5 h-5 text-white" />
                )}
              </div>
              
              {/* Pin bottom triangle */}
              <div className="absolute left-1/2 transform -translate-x-1/2 top-8 w-0 h-0 border-l-4 border-r-4 border-t-8 border-l-transparent border-r-transparent border-t-yellow-400"></div>
            </div>
          </div>
        ))}

        {/* Current location marker (center) */}
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
          <div className="relative">
            <div className="w-16 h-16 rounded-full bg-blue-500 opacity-20 absolute inset-0 animate-pulse"></div>
            <div className="w-4 h-4 rounded-full bg-blue-500 border-4 border-white shadow-lg relative z-10 left-6 top-6"></div>
          </div>
        </div>
      </div>
    </div>
  );
}
