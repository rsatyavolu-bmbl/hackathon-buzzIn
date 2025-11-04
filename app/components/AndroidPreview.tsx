import { useState } from "react";
import { HomeScreen } from "./screens/HomeScreen";
import { ProfileScreen } from "./screens/ProfileScreen";
import { SettingsScreen } from "./screens/SettingsScreen";
import { MapScreen } from "./screens/MapScreen";

type Screen = "profile" | "discover" | "buzzin" | "liked" | "chats";

export function AndroidPreview() {
  const [currentScreen, setCurrentScreen] = useState<Screen>("discover");

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      {/* Android Device Frame */}
      <div className="relative w-full max-w-[400px] h-[800px] bg-black rounded-[3rem] p-3 shadow-2xl">
        {/* Notch */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-6 bg-black rounded-b-2xl z-10"></div>
        
        {/* Screen */}
        <div className="w-full h-full bg-white rounded-[2.5rem] overflow-hidden flex flex-col">
          {/* Status Bar */}
          <div className="h-11 bg-white flex items-center justify-between px-6 pt-2 shrink-0">
            <span className="text-sm">9:41</span>
            <div className="flex gap-1 items-center">
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12.72 2.03C6.63 1.6 1.6 6.63 2.03 12.72 2.39 18.01 7.01 22 12.31 22H16c.55 0 1-.45 1-1s-.45-1-1-1h-3.67c-3.73 0-7.15-2.42-8.08-6.03-1.49-5.8 3.91-11.21 9.71-9.71C17.58 5.18 20 8.6 20 12.33v1.1c0 .79-.71 1.57-1.5 1.57s-1.5-.78-1.5-1.57v-1.1c0-2.76-2.24-5-5-5s-5 2.24-5 5 2.24 5 5 5c1.38 0 2.64-.56 3.54-1.47.65.89 1.77 1.47 2.96 1.47 1.97 0 3.5-1.6 3.5-3.57v-1.1c0-5.3-3.99-9.92-9.28-10.3zM12 15c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"/>
              </svg>
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path d="M15.67 4H14V2h-4v2H8.33C7.6 4 7 4.6 7 5.33v15.33C7 21.4 7.6 22 8.33 22h7.33c.74 0 1.34-.6 1.34-1.33V5.33C17 4.6 16.4 4 15.67 4z"/>
              </svg>
            </div>
          </div>

          {/* Screen Content */}
          <div className="flex-1 overflow-y-auto">
            {currentScreen === "profile" && <ProfileScreen />}
            {currentScreen === "discover" && <HomeScreen />}
            {currentScreen === "buzzin" && <MapScreen />}
            {currentScreen === "liked" && <SettingsScreen />}
            {currentScreen === "chats" && <SettingsScreen />}
          </div>

          {/* Bottom Navigation */}
          <div className="h-16 bg-white border-t border-border flex items-center justify-around shrink-0">
            <button
              onClick={() => setCurrentScreen("profile")}
              className="flex flex-col items-center gap-1 py-2 px-2"
            >
              <svg
                className="w-6 h-6"
                fill={currentScreen === "profile" ? "currentColor" : "none"}
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span className="text-xs">Profile</span>
            </button>
            <button
              onClick={() => setCurrentScreen("discover")}
              className="flex flex-col items-center gap-1 py-2 px-2"
            >
              <svg
                className="w-6 h-6"
                fill={currentScreen === "discover" ? "currentColor" : "none"}
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M9.879 16.121A3 3 0 1012.015 11L11 14H9c0 .768.293 1.536.879 2.121z" />
              </svg>
              <span className="text-xs">Discover</span>
            </button>
            <button
              onClick={() => setCurrentScreen("buzzin")}
              className="flex flex-col items-center gap-1 py-2 px-2"
            >
              <svg
                className="w-6 h-6"
                fill={currentScreen === "buzzin" ? "currentColor" : "none"}
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
              <span className="text-xs">Buzz In</span>
            </button>
            <button
              onClick={() => setCurrentScreen("liked")}
              className="flex flex-col items-center gap-1 py-2 px-2"
            >
              <svg
                className="w-6 h-6"
                fill={currentScreen === "liked" ? "currentColor" : "none"}
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
              <span className="text-xs">Liked You</span>
            </button>
            <button
              onClick={() => setCurrentScreen("chats")}
              className="flex flex-col items-center gap-1 py-2 px-2"
            >
              <svg
                className="w-6 h-6"
                fill={currentScreen === "chats" ? "currentColor" : "none"}
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
              <span className="text-xs">Chats</span>
            </button>
          </div>
        </div>

        {/* Home Button */}
        <div className="absolute bottom-1 left-1/2 -translate-x-1/2 w-32 h-1 bg-slate-300 rounded-full"></div>
      </div>
    </div>
  );
}
