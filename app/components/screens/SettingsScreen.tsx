import { Card } from "../ui/card";
import { Switch } from "../ui/switch";
import { Button } from "../ui/button";
import { ChevronRight } from "lucide-react";

export function SettingsScreen() {
  return (
    <div className="flex flex-col h-full">
      {/* App Bar */}
      <div className="bg-primary text-primary-foreground px-4 py-5">
        <h1>Settings</h1>
      </div>

      {/* Content */}
      <div className="flex-1 p-4 space-y-6 overflow-y-auto">
        <div className="space-y-3">
          <h3 className="px-2">General</h3>
          <Card className="divide-y">
            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <div className="text-left">
                  <p>Account</p>
                  <span className="text-sm text-muted-foreground">Manage your account</span>
                </div>
              </div>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>

            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <div className="text-left">
                  <p>Privacy</p>
                  <span className="text-sm text-muted-foreground">Privacy settings</span>
                </div>
              </div>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>

            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                </div>
                <div className="text-left">
                  <p>Notifications</p>
                  <span className="text-sm text-muted-foreground">Configure alerts</span>
                </div>
              </div>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>
          </Card>
        </div>

        <div className="space-y-3">
          <h3 className="px-2">Preferences</h3>
          <Card className="divide-y">
            <div className="flex items-center justify-between p-4">
              <div className="flex-1">
                <p>Dark Mode</p>
                <span className="text-sm text-muted-foreground">Enable dark theme</span>
              </div>
              <Switch />
            </div>

            <div className="flex items-center justify-between p-4">
              <div className="flex-1">
                <p>Push Notifications</p>
                <span className="text-sm text-muted-foreground">Receive updates</span>
              </div>
              <Switch defaultChecked />
            </div>

            <div className="flex items-center justify-between p-4">
              <div className="flex-1">
                <p>Auto-sync</p>
                <span className="text-sm text-muted-foreground">Sync data automatically</span>
              </div>
              <Switch defaultChecked />
            </div>
          </Card>
        </div>

        <div className="space-y-3">
          <h3 className="px-2">Support</h3>
          <Card className="divide-y">
            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <span>Help Center</span>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>
            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <span>Contact Support</span>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>
            <button className="w-full flex items-center justify-between p-4 hover:bg-accent/50 transition-colors">
              <span>About</span>
              <ChevronRight className="w-5 h-5 text-muted-foreground" />
            </button>
          </Card>
        </div>

        <Button variant="destructive" className="w-full">
          Sign Out
        </Button>

        <div className="text-center text-sm text-muted-foreground pb-4">
          Version 1.0.0
        </div>
      </div>
    </div>
  );
}
