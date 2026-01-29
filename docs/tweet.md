# Tweet

Added a full LibGDX particle system to Neon Signal today!

- Pooled ParticleEffectManager for .p files from GDX Particle Editor
- ParticleEmitterComponent for placing effects in Tiled
- Velocity-based rotation for impact effects (bullet sparks now spray opposite to travel direction!)
- Back/front render layers with viewport culling
- Configurable audio falloff distance

The particle angle rotation was tricky - turns out `relative: true` in .p files makes angles relative, not absolute. Once fixed, everything clicked into place.

#gamedev #libgdx #indiedev #NeonSignal
