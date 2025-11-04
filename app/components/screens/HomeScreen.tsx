import { useState } from "react";
import { ImageWithFallback } from "../figma/ImageWithFallback";
import { X, Heart, Info, MapPin } from "lucide-react";
import { Badge } from "../ui/badge";

interface Profile {
  id: number;
  name: string;
  age: number;
  location: string;
  distance: string;
  bio: string;
  occupation: string;
  interests: string[];
  photos: string[];
}

const profiles: Profile[] = [
  {
    id: 1,
    name: "Sarah",
    age: 26,
    location: "Brooklyn, NY",
    distance: "2 miles away",
    bio: "Coffee enthusiast ☕ | Travel addict ✈️ | Dog mom to the cutest golden retriever 🐕",
    occupation: "Marketing Manager",
    interests: ["Travel", "Photography", "Yoga", "Coffee"],
    photos: [
      "https://images.unsplash.com/photo-1690444963408-9573a17a8058?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHdvbWFuJTIwc21pbGluZ3xlbnwxfHx8fDE3NjIwMzAzMDh8MA&ixlib=rb-4.1.0&q=80&w=1080",
      "https://images.unsplash.com/photo-1638280219567-be72272eb375?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3b21hbiUyMGhhcHB5JTIwY2FzdWFsfGVufDF8fHx8MTc2MjEzNTM1OHww&ixlib=rb-4.1.0&q=80&w=1080",
      "https://images.unsplash.com/photo-1587723909168-8330a66d8ae7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwZXJzb24lMjB0cmF2ZWwlMjBhZHZlbnR1cmV8ZW58MXx8fHwxNzYyMTM1MzU5fDA&ixlib=rb-4.1.0&q=80&w=1080",
    ],
  },
  {
    id: 2,
    name: "Michael",
    age: 29,
    location: "Manhattan, NY",
    distance: "3 miles away",
    bio: "Software engineer by day, aspiring chef by night 👨‍💻🍳 | Gym rat | Always down for brunch",
    occupation: "Software Engineer",
    interests: ["Cooking", "Fitness", "Hiking", "Music"],
    photos: [
      "https://images.unsplash.com/photo-1680557345345-6f9ef109d252?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMG1hbiUyMG91dGRvb3J8ZW58MXx8fHwxNzYyMDczNjQ2fDA&ixlib=rb-4.1.0&q=80&w=1080",
      "https://images.unsplash.com/photo-1690444963408-9573a17a8058?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHdvbWFuJTIwc21pbGluZ3xlbnwxfHx8fDE3NjIwMzAzMDh8MA&ixlib=rb-4.1.0&q=80&w=1080",
    ],
  },
];

export function HomeScreen() {
  const [currentProfileIndex, setCurrentProfileIndex] = useState(0);
  const [currentPhotoIndex, setCurrentPhotoIndex] = useState(0);
  const [showInfo, setShowInfo] = useState(false);

  const currentProfile = profiles[currentProfileIndex];
  const totalPhotos = currentProfile.photos.length;

  const handlePhotoTap = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const halfWidth = rect.width / 2;

    if (x < halfWidth) {
      // Tapped left side - previous photo
      setCurrentPhotoIndex((prev) => (prev > 0 ? prev - 1 : prev));
    } else {
      // Tapped right side - next photo
      setCurrentPhotoIndex((prev) => (prev < totalPhotos - 1 ? prev + 1 : prev));
    }
  };

  const handleReject = () => {
    // Move to next profile
    if (currentProfileIndex < profiles.length - 1) {
      setCurrentProfileIndex((prev) => prev + 1);
      setCurrentPhotoIndex(0);
      setShowInfo(false);
    }
  };

  const handleLike = () => {
    // Move to next profile
    if (currentProfileIndex < profiles.length - 1) {
      setCurrentProfileIndex((prev) => prev + 1);
      setCurrentPhotoIndex(0);
      setShowInfo(false);
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b shrink-0">
        <div className="w-8"></div>
        <svg className="h-8" viewBox="0 0 120 32" fill="none">
          <rect width="120" height="32" rx="4" fill="#FFC629"/>
          <text x="60" y="22" textAnchor="middle" fill="#fff" fontSize="18" fontWeight="bold">bumble</text>
        </svg>
        <button className="p-2">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
        </button>
      </div>

      {/* Profile Card */}
      <div className="flex-1 overflow-hidden">
        <div className="relative h-full bg-white shadow-2xl overflow-hidden">
          {/* Photo with tap zones */}
          <div className="relative h-full" onClick={handlePhotoTap}>
            <ImageWithFallback
              src={currentProfile.photos[currentPhotoIndex]}
              alt={currentProfile.name}
              className="w-full h-full object-cover"
            />

            {/* Photo indicators */}
            <div className="absolute top-4 left-0 right-0 flex gap-2 px-4">
              {currentProfile.photos.map((_, index) => (
                <div
                  key={index}
                  className="flex-1 h-1 rounded-full bg-white/30 overflow-hidden"
                >
                  <div
                    className={`h-full bg-white transition-all duration-300 ${
                      index === currentPhotoIndex ? "w-full" : "w-0"
                    }`}
                  />
                </div>
              ))}
            </div>

            {/* Info button */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                setShowInfo(!showInfo);
              }}
              className="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/90 flex items-center justify-center shadow-lg"
            >
              <Info className="w-5 h-5" />
            </button>

            {/* Profile Info Overlay - Above action buttons */}
            <div className="absolute bottom-32 left-0 right-0 px-6 pb-4 text-white">
              <div className="flex items-baseline gap-2 drop-shadow-lg">
                <h2 className="text-white">{currentProfile.name}</h2>
                <span className="text-white">{currentProfile.age}</span>
              </div>
              
              <div className="flex items-center gap-1 text-white/90 drop-shadow-lg">
                <MapPin className="w-4 h-4" />
                <span className="text-sm">{currentProfile.location}</span>
              </div>
            </div>

            {/* Action Buttons - Overlaid on image */}
            <div className="absolute bottom-8 left-0 right-0 px-8 flex items-center justify-between z-10">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleReject();
                }}
                className="w-14 h-14 rounded-full bg-white shadow-2xl flex items-center justify-center hover:scale-110 transition-transform"
              >
                <X className="w-6 h-6 text-red-500" strokeWidth={3} />
              </button>
              
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleLike();
                }}
                className="w-14 h-14 rounded-full bg-white shadow-2xl flex items-center justify-center hover:scale-110 transition-transform"
              >
                <Heart className="w-6 h-6" strokeWidth={3} fill="#FFC629" style={{ color: '#FFC629' }} />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
