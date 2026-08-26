-- level1_intro2

move_camera_to { target = "barman" }
set_parent { entity = "ufo_beam", parent = "ufo" }

parallel {
    walk_to { entity = "ufo", target = "t2", exact = true },

    sequence {
        say {
            name = "barman", start = true, finish = true,
            text = [[
What's ya flavor,
my man?]]
        },
        say {
            name = "coolfox", left = true, start = true,
            text = "I'd like to have a..."
        },
        say {
            name = "coolfox", left = true, finish = true,
            text = "What the..."
        },
    },

    sequence {
        delay { duration = 2.6 },
        play_music { name = "boss_battle" },
    },
}

move_camera_to { target = "t2", speed = 512 }
set_visible { entity = "ufo_beam", visible = true }
set_movement_active { entity = "mrs_coolfox", active = true }

say {
    name = "mrs_coolfox", start = true, finish = true,
    text = "Aaah... Help me!"
}

set_parent { entity = "mrs_coolfox", parent = "ufo" }
delay { duration = 1 }
set_visible { entity = "ufo_beam", visible = false }
set_movement_active { entity = "ufo", active = true, finish_on_skip = false }
move_camera_to { target = "player" }
