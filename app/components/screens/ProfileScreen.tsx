import { Button } from "../ui/button";
import { Card } from "../ui/card";
import { Avatar, AvatarFallback } from "../ui/avatar";
import { Badge } from "../ui/badge";

export function ProfileScreen() {
  return (
    <div className="flex flex-col h-full">
      {/* Header with gradient */}
      <div className="bg-gradient-to-br from-primary to-primary/80 px-4 py-8 text-primary-foreground">
        <div className="flex items-center gap-4">
          <Avatar className="w-20 h-20">
            <AvatarFallback>JD</AvatarFallback>
          </Avatar>
          <div className="flex-1">
            <h2 className="text-white">John Doe</h2>
            <p className="text-white/80">john.doe@example.com</p>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 p-4 bg-white border-b">
        <div className="text-center">
          <div className="text-primary">128</div>
          <div className="text-sm text-muted-foreground">Posts</div>
        </div>
        <div className="text-center">
          <div className="text-primary">1.2k</div>
          <div className="text-sm text-muted-foreground">Followers</div>
        </div>
        <div className="text-center">
          <div className="text-primary">342</div>
          <div className="text-sm text-muted-foreground">Following</div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 p-4 space-y-4 overflow-y-auto">
        <Card className="p-4 space-y-3">
          <div className="flex items-center justify-between">
            <h3>About</h3>
            <Button variant="ghost" size="sm">Edit</Button>
          </div>
          <p className="text-muted-foreground">
            Software developer passionate about mobile apps and UI design.
          </p>
          <div className="flex flex-wrap gap-2">
            <Badge variant="secondary">React</Badge>
            <Badge variant="secondary">Android</Badge>
            <Badge variant="secondary">Kotlin</Badge>
            <Badge variant="secondary">UI/UX</Badge>
          </div>
        </Card>

        <div className="space-y-3">
          <h3>Achievements</h3>
          <div className="grid grid-cols-4 gap-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="aspect-square bg-gradient-to-br from-primary/20 to-primary/10 rounded-lg flex items-center justify-center">
                <svg className="w-8 h-8 text-primary" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
                </svg>
              </div>
            ))}
          </div>
        </div>

        <Card className="p-4 space-y-3">
          <h3>Activity</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between py-2 border-b last:border-0">
              <span>Total projects</span>
              <span className="text-muted-foreground">24</span>
            </div>
            <div className="flex items-center justify-between py-2 border-b last:border-0">
              <span>Completed tasks</span>
              <span className="text-muted-foreground">156</span>
            </div>
            <div className="flex items-center justify-between py-2 border-b last:border-0">
              <span>Active hours</span>
              <span className="text-muted-foreground">2,340</span>
            </div>
          </div>
        </Card>

        <Button variant="outline" className="w-full">View Full Profile</Button>
      </div>
    </div>
  );
}
