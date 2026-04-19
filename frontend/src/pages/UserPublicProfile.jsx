import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { user as userApi } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { User, Star } from 'lucide-react';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';

export default function UserPublicProfile() {
  const { id } = useParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userApi.getPublic(id).then(setProfile).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (!profile) return <p className="text-center py-16 text-muted-foreground">Пользователь не найден</p>;

  return (
    <div>
      <PageHeader title="Профиль пользователя" />
      <Card className="bg-card border-border max-w-lg">
        <CardContent className="p-6">
          <div className="flex items-center gap-4 mb-4">
            <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center">
              <User className="w-8 h-8 text-primary" />
            </div>
            <div>
              <h2 className="text-xl font-bold">{profile.username}</h2>
              <p className="text-sm text-muted-foreground">{profile.role || ''}</p>
            </div>
          </div>
          {profile.rating != null && (
            <div className="flex items-center gap-2">
              <Star className="w-4 h-4 fill-warning text-warning" />
              <span className="text-sm font-medium">{profile.rating?.toFixed(1)}</span>
            </div>
          )}
          {profile.registeredAt && (
            <p className="text-xs text-muted-foreground mt-2">На платформе с {new Date(profile.registeredAt).toLocaleDateString()}</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}